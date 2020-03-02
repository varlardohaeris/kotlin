// !LANGUAGE: +NewInference
// !DIAGNOSTICS: -UNUSED_VARIABLE -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_PARAMETER -UNUSED_EXPRESSION -NOTHING_TO_INLINE
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS SPEC TEST (POSITIVE)
 *
 * SPEC VERSION: 0.1-278
 * PLACE: overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 4
 * overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 5
 * overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 6
 * overload-resolution, building-the-overload-candidate-set-ocs, call-without-an-explicit-receiver -> paragraph 5 -> sentence 7
 * NUMBER: 1
 * DESCRIPTION: Top-level non-extension functions: Callables explicitly imported into the current file
 */


// FILE: Lib.kt
package libPackage

public fun <T> emptyArray(): Array<T> = TODO()

// FILE: Lib.kt
package libPackageExplicit

public fun <T> emptyArray(): Array<T> = TODO()

// FILE: LibTestsPack.kt
package tests

public fun <T> emptyArray(): Array<T> = TODO()

// FILE: TestCase2.kt
package tests
import libPackage.*
import libPackageExplicit.emptyArray

// TESTCASE NUMBER: 1
fun case1() {
    <!DEBUG_INFO_AS_CALL("fqName: libPackageExplicit.emptyArray; typeCall: function")!>emptyArray<Int>()<!>
}