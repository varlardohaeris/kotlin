// !DIAGNOSTICS: -UNUSED_PARAMETER

interface First
interface Second
interface Third
interface Fourth

fun chained1(arg: First) = run {
    if (arg !is Second) throw Exception()
    arg
}.let { third ->
    if (third !is Third) throw Exception()
    third
}

fun chained2(arg: First) = run {
    if (arg !is Second) throw Exception()
    arg
}.let { third ->
    if (third !is Third) throw Exception()
    third
}.let { fourth ->
    if (fourth !is Fourth) throw Exception()
    fourth
}

fun test(arg: First) {
    <!DEBUG_INFO_EXPRESSION_TYPE("First")!>chained1(arg)<!>
    <!DEBUG_INFO_EXPRESSION_TYPE("First")!>chained2(arg)<!>
}
