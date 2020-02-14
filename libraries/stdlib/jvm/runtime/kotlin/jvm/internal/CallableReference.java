/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal;

import kotlin.SinceKotlin;
import kotlin.jvm.KotlinReflectionNotSupportedError;
import kotlin.reflect.*;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * A superclass for all classes generated by Kotlin compiler for callable references.
 *
 * All methods from KCallable should be implemented here and should delegate to the actual implementation, loaded dynamically
 * and stored in the {@link CallableReference#reflected} field.
 */
@SuppressWarnings({"unchecked", "NullableProblems", "rawtypes"})
public abstract class CallableReference implements KCallable, Serializable {
    // This field is not volatile intentionally:
    // 1) It's fine if the value is computed multiple times in different threads;
    // 2) An uninitialized value cannot be observed in this field from other thread because only already initialized or safely initialized
    //    objects are written to it. The latter is guaranteed because both KFunctionImpl and KPropertyImpl have at least one final field.
    private transient KCallable reflected;

    @SinceKotlin(version = "1.1")
    protected final Object receiver;

    @SinceKotlin(version = "1.4")
    private final Class owner;

    @SinceKotlin(version = "1.4")
    private final String name;

    @SinceKotlin(version = "1.4")
    private final String signature;

    @SinceKotlin(version = "1.4")
    private final boolean isTopLevel;

    @SinceKotlin(version = "1.1")
    public static final Object NO_RECEIVER = NoReceiver.INSTANCE;

    @SinceKotlin(version = "1.2")
    private static class NoReceiver implements Serializable {
        private static final NoReceiver INSTANCE = new NoReceiver();

        private Object readResolve() throws ObjectStreamException {
            return INSTANCE;
        }
    }

    public CallableReference() {
        this(NO_RECEIVER);
    }

    @SinceKotlin(version = "1.1")
    protected CallableReference(Object receiver) {
        this(receiver, null, null, null, 0);
    }

    /**
     * @param flags Bitmask where bits represent the following flags:<br/>
     *              <li><ul>0 - the owner of this reference is a package, not a class.</ul></li>
     */
    @SinceKotlin(version = "1.4")
    protected CallableReference(Object receiver, Class owner, String name, String signature, int flags) {
        this.receiver = receiver;
        this.owner = owner;
        this.name = name;
        this.signature = signature;
        this.isTopLevel = (flags & 1) == 1;
    }

    protected abstract KCallable computeReflected();

    @SinceKotlin(version = "1.1")
    public Object getBoundReceiver() {
        return receiver;
    }

    @SinceKotlin(version = "1.1")
    public KCallable compute() {
        KCallable result = reflected;
        if (result == null) {
            result = computeReflected();
            reflected = result;
        }
        return result;
    }

    @SinceKotlin(version = "1.1")
    protected KCallable getReflected() {
        KCallable result = compute();
        if (result == this) {
            throw new KotlinReflectionNotSupportedError();
        }
        return result;
    }

    // The following methods provide the information identifying this callable, which is used by the reflection implementation and
    // by equals/hashCode/toString. For callable references compiled prior to 1.4, these methods were overridden in each subclass
    // (each anonymous class generated for a callable reference).

    /**
     * @return the class or package where the callable should be located, usually specified on the LHS of the '::' operator
     */
    public KDeclarationContainer getOwner() {
        return owner == null ? null :
               isTopLevel ? Reflection.getOrCreateKotlinPackage(owner) : Reflection.getOrCreateKotlinClass(owner);
    }

    /**
     * @return Kotlin name of the callable, the one which was declared in the source code (@JvmName doesn't change it)
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return JVM signature of the callable, e.g. "println(Ljava/lang/Object;)V". If this is a property reference,
     * returns the JVM signature of its getter, e.g. "getFoo(Ljava/lang/String;)I". If the property has no getter in the bytecode
     * (e.g. private property in a class), it's still the signature of the imaginary default getter that would be generated otherwise.
     *
     * Note that technically the signature itself is not even used as a signature per se in reflection implementation,
     * but only as a unique and unambiguous way to map a function/property descriptor to a string.
     */
    public String getSignature() {
        return signature;
    }

    // The following methods are the delegating implementations of reflection functions. They are called when you're using reflection
    // on a callable reference. Without kotlin-reflect.jar in the classpath, getReflected() throws an exception.

    @Override
    public List<KParameter> getParameters() {
        return getReflected().getParameters();
    }

    @Override
    public KType getReturnType() {
        return getReflected().getReturnType();
    }

    @Override
    public List<Annotation> getAnnotations() {
        return getReflected().getAnnotations();
    }

    @Override
    @SinceKotlin(version = "1.1")
    public List<KTypeParameter> getTypeParameters() {
        return getReflected().getTypeParameters();
    }

    @Override
    public Object call(Object... args) {
        return getReflected().call(args);
    }

    @Override
    public Object callBy(Map args) {
        return getReflected().callBy(args);
    }

    @Override
    @SinceKotlin(version = "1.1")
    public KVisibility getVisibility() {
        return getReflected().getVisibility();
    }

    @Override
    @SinceKotlin(version = "1.1")
    public boolean isFinal() {
        return getReflected().isFinal();
    }

    @Override
    @SinceKotlin(version = "1.1")
    public boolean isOpen() {
        return getReflected().isOpen();
    }

    @Override
    @SinceKotlin(version = "1.1")
    public boolean isAbstract() {
        return getReflected().isAbstract();
    }

    @Override
    @SinceKotlin(version = "1.3")
    public boolean isSuspend() {
        return getReflected().isSuspend();
    }
}
