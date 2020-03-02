// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-278
 * PLACE: overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 7
 * NUMBER: 1
 * DESCRIPTION: Top-level non-extension functions: Implicitly imported callables
 */

// FILE: Lib.kt
package libPackage

private fun <T> emptyArray(): Array<T> = TODO()

// FILE: TestCase.kt
package tests
import libPackage.* //nothing to import, function emptyArray is private

// TESTCASE NUMBER: 1
fun case1() {
    <!DEBUG_INFO_AS_CALL("fqName: kotlin.emptyArray; typeCall: inline function")!>emptyArray<Int>()<!>
}