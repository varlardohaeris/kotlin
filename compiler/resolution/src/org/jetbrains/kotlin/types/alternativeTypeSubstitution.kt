/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.types.typeUtil.contains

fun substituteAlternativesInPublicType(type: KotlinType): UnwrappedType {
    if (!type.contains { it.constructor is IntersectionTypeConstructor })
        return type.unwrap()

    return doReplace(type.unwrap())
}

private fun doReplace(type: UnwrappedType): UnwrappedType {
    if (type is ErrorType) return type

    val constructor = type.constructor
    if (constructor is IntersectionTypeConstructor) {
        constructor.getTypeWithoutSmartCast()?.let { withoutLastSmartCast ->
            return doReplace(withoutLastSmartCast.unwrap())
                .inheritEnhancement(type)
        }
        constructor.getAlternativeType()?.let { alternative ->
            return doReplace(alternative.unwrap())
                .inheritEnhancement(type)
        }
    }

    return when (val unwrappedType = type.unwrap()) {
        is SimpleType -> unwrappedType.updateArguments().inheritEnhancement(type)
        is FlexibleType -> KotlinTypeFactory.flexibleType(
            unwrappedType.lowerBound.updateArguments(),
            unwrappedType.upperBound.updateArguments(),
        ).inheritEnhancement(type)
    }
}

private fun SimpleType.updateArguments(): SimpleType {
    return replace(arguments.map { replaceProjection(it) })
}

private fun replaceProjection(projection: TypeProjection): TypeProjection {
    if (projection.isStarProjection) return projection

    return TypeProjectionImpl(projection.projectionKind, doReplace(projection.type.unwrap()))
}
