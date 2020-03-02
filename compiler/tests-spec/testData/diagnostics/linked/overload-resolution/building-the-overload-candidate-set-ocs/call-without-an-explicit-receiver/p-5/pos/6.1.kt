// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-278
 * PLACE: overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 6
 * overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 7
 * NUMBER: 1
 * DESCRIPTION: Top-level non-extension functions: Callables star-imported into the current file;
 */


// FILE: Lib.kt
package libPackage

public fun <T> emptyArray(): Array<T> = TODO()

// FILE: TestCase2.kt
package tests
import libPackage.*

// TESTCASE NUMBER: 1
fun case1() {
    <!DEBUG_INFO_AS_CALL("fqName: libPackage.emptyArray; typeCall: function")!>emptyArray<Int>()<!>
}