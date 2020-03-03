// !DIAGNOSTICS: -UNUSED_PARAMETER

interface Base
interface Base2
interface One : Base, Base2
interface Two : Base, Base2

object O1 : One
object O2 : Two

fun <S> intersect(vararg elements: S): S = TODO()

fun intersectAfterSmartCast(arg: Base, arg2: Base) = intersect(
    run {
        if (arg !is One) throw Exception()
        <!DEBUG_INFO_SMARTCAST!>arg<!>
    },
    run {
        if (arg2 !is Two) throw Exception()
        <!DEBUG_INFO_SMARTCAST!>arg2<!>
    }
)

fun <S> argOrFn(arg: S, fn: () -> S): S = TODO()

fun intersectArgWithSmartCastFromLambda(arg: One, arg2: Base) = argOrFn(arg) {
    if (arg2 !is Two) throw Exception()
    arg2
}

fun test() {
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>intersectAfterSmartCast(O1, O2)<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.Any")!>intersectArgWithSmartCastFromLambda(O1, O2)<!>
}