/*
 * KOTLIN CODEGEN BOX SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-100
 * PLACE: expressions, constant-literals, boolean-literals -> paragraph 1 -> sentence 2
 * NUMBER: 16
 * DESCRIPTION: The use of Boolean literals as the identifier (with backtick) in the fileAnnotationComplex.
 * NOTE: this test data is generated by FeatureInteractionTestDataGenerator. DO NOT MODIFY CODE MANUALLY!
 * HELPERS: reflect
 */

@file:[org.jetbrains.`true`.`false`() `true`]

package org.jetbrains.`true`

@Target(AnnotationTarget.FILE)
annotation class `false`

@Target(AnnotationTarget.FILE)
annotation class `true`

fun box(): String? {
    if (!checkFileAnnotations("org.jetbrains.true._2_16Kt", listOf("org.jetbrains.true.false", "true"))) return null
    if (!checkPackageName("org.jetbrains.true._2_16Kt", "org.jetbrains.true")) return null
    if (!checkClassName(`false`::class, "org.jetbrains.true.false")) return null

    return "OK"
}
