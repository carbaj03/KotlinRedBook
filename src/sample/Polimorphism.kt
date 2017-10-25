package sample

fun findFirst(ss: Array<String>, key: String): Int {
    tailrec fun loop(n: Int): Int =
            if (n >= ss.size) -1
            else if (ss[n] == key) n
            else loop(n + 1)
    return loop(0)
}

fun <A> findFirst(ars: Array<A>, p: (A) -> Boolean): Int {
    tailrec fun loop(n: Int): Int =
            if (n >= ars.size) -1
            else if (p(ars[n])) n
            else loop(n + 1)
    return loop(0)
}

fun <A> isSorted(ars: Array<A>, ordered: (A, A) -> Boolean): Boolean {
    tailrec fun loop(n: Int): Boolean =
            if (n >= ars.size - 1) true
            else if (ordered(ars[n], ars[n + 1])) false
            else loop(n + 1)
    return loop(0)
}

fun <A, B, C> partial1(a: A, f: (A, B) -> C): (B) -> C =
        { b -> f(a, b) }

fun <A, B, C> curry(f: (A, B) -> C): (A) -> (B) -> C =
        { a: A -> { b: B -> f(a, b) } }

fun <A, B, C> uncurry(f: (A) -> ((B) -> C)): (A, B) -> C =
        { a: A, b: B -> f(a)(b) }

fun <A, B, C> composition(f: (B) -> C, g: (A) -> B): (A) -> C =
        { a: A -> f(g(a)) }

fun main(args: Array<String>) {
    println(isSorted(arrayOf(1, 2, 3, 1), { x, y -> x > y }))
}
