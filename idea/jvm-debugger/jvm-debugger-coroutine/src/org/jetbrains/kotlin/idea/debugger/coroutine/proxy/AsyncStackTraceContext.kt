/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.debugger.coroutine.proxy

import com.intellij.debugger.DebuggerContext
import com.intellij.debugger.engine.JavaValue
import com.intellij.debugger.engine.evaluation.EvaluateException
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl
import com.intellij.debugger.jdi.GeneratedLocation
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.xdebugger.frame.XNamedValue
import com.sun.jdi.*
import org.jetbrains.kotlin.codegen.coroutines.CONTINUATION_VARIABLE_NAME
import org.jetbrains.kotlin.idea.debugger.SUSPEND_LAMBDA_CLASSES
import org.jetbrains.kotlin.idea.debugger.coroutine.data.CoroutineStackFrameItem
import org.jetbrains.kotlin.idea.debugger.coroutine.data.DefaultCoroutineStackFrameItem
import org.jetbrains.kotlin.idea.debugger.coroutine.util.*
import org.jetbrains.kotlin.idea.debugger.evaluate.ExecutionContext
import org.jetbrains.kotlin.idea.debugger.isSubtype
import org.jetbrains.kotlin.idea.debugger.safeVisibleVariableByName

class AsyncStackTraceContext(
    val context: ExecutionContext,
    val method: Method
) {
    val log by logger

    private companion object {
        const val DEBUG_METADATA_KT = "kotlin.coroutines.jvm.internal.DebugMetadataKt"
    }

    fun getAsyncStackTraceIfAny(): List<CoroutineStackFrameItem> {
        val continuation = locateContinuation() ?: return emptyList()
        val frames = mutableListOf<CoroutineStackFrameItem>()
        try {
            collectFramesRecursively(continuation, frames)
        } catch (e: Exception) {
            log.error("Error while looking for variables.", e)
        }
        return frames
    }

    private fun locateContinuation(): ObjectReference? {
        val continuation: ObjectReference?
        if (isInvokeSuspendMethod(method)) {
            continuation = context.frameProxy.thisObject() ?: return null
            if (!isSuspendLambda(continuation.referenceType()))
                return null
        } else if (isContinuationProvider(method)) {
            val frameProxy = context.frameProxy
            val continuationVariable = frameProxy.safeVisibleVariableByName(CONTINUATION_VARIABLE_NAME) ?: return null
            continuation = frameProxy.getValue(continuationVariable) as? ObjectReference ?: return null
            context.keepReference(continuation)
        } else {
            continuation = null
        }
        return continuation
    }

    private fun isInvokeSuspendMethod(method: Method): Boolean =
        method.name() == "invokeSuspend" && method.signature() == "(Ljava/lang/Object;)Ljava/lang/Object;"

    private fun isContinuationProvider(method: Method): Boolean =
        "Lkotlin/coroutines/Continuation;)" in method.signature()

    private fun isSuspendLambda(referenceType: ReferenceType): Boolean =
        SUSPEND_LAMBDA_CLASSES.any { referenceType.isSubtype(it) }

    private fun collectFramesRecursively(continuation: ObjectReference, consumer: MutableList<CoroutineStackFrameItem>) {
        val continuationType = continuation.referenceType() as? ClassType ?: return
        val baseContinuationSupertype = findBaseContinuationSuperSupertype(continuationType) ?: return

        val debugMetadataKtType = debugMetadataKtType()
        if (debugMetadataKtType is ClassType) {
            val location = createLocation(continuation, debugMetadataKtType)

            location?.let {
                val spilledVariables = getSpilledVariables(continuation, debugMetadataKtType) ?: emptyList()
                consumer.add(DefaultCoroutineStackFrameItem(location, spilledVariables))
            }

            val completionField = baseContinuationSupertype.fieldByName("completion") ?: return
            val completion = continuation.getValue(completionField) as? ObjectReference ?: return
            collectFramesRecursively(completion, consumer)
        }
    }

    fun debugMetadataKtType(): ClassType? {
        val debugMetadataKtType = context.findClassSafe(DEBUG_METADATA_KT)
        if (debugMetadataKtType != null) {
            return debugMetadataKtType
        } else {
            log.warn("Continuation information found but no $DEBUG_METADATA_KT class exists. Please check kotlin-stdlib version.")
        }
        return null
    }

    private fun createLocation(continuation: ObjectReference, debugMetadataKtType: ClassType): GeneratedLocation? {
        val instance = invokeGetStackTraceElement(continuation, debugMetadataKtType) ?: return null
        val className = context.invokeMethodAsString(instance, "getClassName") ?: return null
        val methodName = context.invokeMethodAsString(instance, "getMethodName") ?: return null
        val lineNumber = context.invokeMethodAsInt(instance, "getLineNumber")?.takeIf {
            it >= 0
        } ?: return null
        val locationClass = context.findClassSafe(className) ?: return null
        return GeneratedLocation(context.debugProcess, locationClass, methodName, lineNumber)
    }

    private fun invokeGetStackTraceElement(continuation: ObjectReference, debugMetadataKtType: ClassType): ObjectReference? {
        val stackTraceElement =
            context.invokeMethodAsObject(debugMetadataKtType, "getStackTraceElement", continuation) ?: return null

        stackTraceElement.referenceType().takeIf { it.name() == StackTraceElement::class.java.name } ?: return null

        context.keepReference(stackTraceElement)
        return stackTraceElement
    }

    fun getSpilledVariables(continuation: ObjectReference): List<XNamedValue>? {
        debugMetadataKtType()?.let {
            return getSpilledVariables(continuation, it)
        }
        return null
    }

    fun getSpilledVariables(continuation: ObjectReference, debugMetadataKtType: ClassType): List<XNamedValue>? {
        val rawSpilledVariables =
            context.invokeMethodAsArray(
                debugMetadataKtType,
                "getSpilledVariableFieldMapping",
                "(Lkotlin/coroutines/jvm/internal/BaseContinuationImpl;)[Ljava/lang/String;",
                continuation
            ) ?: return null

        context.keepReference(rawSpilledVariables)

        val length = rawSpilledVariables.length() / 2
        val spilledVariables = ArrayList<XNamedValue>(length)

        for (index in 0 until length) {
            val fieldName = (rawSpilledVariables.getValue(2 * index) as? StringReference)?.value() ?: continue
            val variableName = (rawSpilledVariables.getValue(2 * index + 1) as? StringReference)?.value() ?: continue
            val field = continuation.referenceType().fieldByName(fieldName) ?: continue

            val valueDescriptor = object : ValueDescriptorImpl(context.project) {
                override fun calcValueName() = variableName
                override fun calcValue(evaluationContext: EvaluationContextImpl?) = continuation.getValue(field)
                override fun getDescriptorEvaluation(context: DebuggerContext?) =
                    throw EvaluateException("Spilled variable evaluation is not supported")
            }

            spilledVariables += JavaValue.create(
                null,
                valueDescriptor,
                context.evaluationContext,
                context.debugProcess.xdebugProcess!!.nodeManager,
                false
            )
        }

        return spilledVariables
    }

    private tailrec fun findBaseContinuationSuperSupertype(type: ClassType): ClassType? {
        if (type.name() == "kotlin.coroutines.jvm.internal.BaseContinuationImpl") {
            return type
        }
        return findBaseContinuationSuperSupertype(type.superclass() ?: return null)
    }
}

