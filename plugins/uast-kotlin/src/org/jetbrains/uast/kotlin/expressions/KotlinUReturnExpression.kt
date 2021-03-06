/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.uast.kotlin

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtReturnExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UReturnExpression

class KotlinUReturnExpression(
        override val sourcePsi: KtReturnExpression,
        givenParent: UElement?
) : KotlinAbstractUExpression(givenParent), UReturnExpression, KotlinUElementWithType {
    override val returnExpression by lz { KotlinConverter.convertOrNull(sourcePsi.returnedExpression, this) }
}

class KotlinUImplicitReturnExpression(
    givenParent: UElement?
) : KotlinAbstractUExpression(givenParent), UReturnExpression, KotlinUElementWithType {
    override val psi: PsiElement?
        get() = null

    override lateinit var returnExpression: UExpression
        internal set

}