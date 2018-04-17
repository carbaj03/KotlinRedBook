package chapter3

import chapter3.ListK.Companion.add1
import chapter3.ListK.Companion.appendViaFoldLeft
import chapter3.ListK.Companion.appendViaFoldRight
import chapter3.ListK.Companion.concat
import chapter3.ListK.Companion.dropWhile
import chapter3.ListK.Companion.filterLocalMutation
import chapter3.ListK.Companion.foldRight
import chapter3.ListK.Companion.reverse


sealed class ListK<out A> {
    companion object {
        fun <A> emptyListK(): ListK<A> =
                invoke()

        fun sum(xs: ListK<Int>): Int =
                when (xs) {
                    Nil -> 0
                    is Cons -> xs.head + sum(xs.tail)
                }

        fun product(xs: ListK<Double>): Double =
                when (xs) {
                    Nil -> 1.0
                    is Cons -> when (xs.head) {
                        0.0 -> 0.0
                        else -> xs.head * product(xs.tail)
                    }
                }

        operator fun <A> invoke(vararg ars: A): ListK<A> =
                if (ars.isEmpty()) Nil
                else Cons(ars.first(), invoke(*ars.copyOfRange(1, ars.size)))

        operator fun <A> invoke(ars: List<A>): ListK<A> =
                if (ars.isEmpty()) Nil
                else Cons(ars[0], invoke(ars.drop(1)))

        fun <A> tail(xs: ListK<A>): ListK<A> =
                when (xs) {
                    Nil -> Nil
                    is Cons -> xs.tail
                }

        fun <A> setHead(x: A, xs: ListK<A>): ListK<A> =
                when (xs) {
                    Nil -> Nil
                    is Cons -> Cons(x, xs)
                }

        fun <A> drop(xs: ListK<A>, n: Int): ListK<A> =
                if (n <= 0) xs
                else when (xs) {
                    Nil -> Nil
                    is Cons -> drop(xs.tail, n - 1)
                }

        fun <A> dropWhile(xs: ListK<A>, f: (A) -> Boolean): ListK<A> =
                when (xs) {
                    is Cons -> if (f(xs.head)) dropWhile(xs.tail, f) else xs
                    else -> xs
                }

        fun <A> append(xs1: ListK<A>, xs2: ListK<A>): ListK<A> =
                when (xs1) {
                    Nil -> xs2
                    is Cons -> Cons(xs1.head, append(xs1.tail, xs2))
                }

        fun <A> init(xs: ListK<A>): ListK<A> =
                when (xs) {
                    Nil -> Nil
                    is Cons -> when (xs.tail) {
                        Nil -> Nil
                        is Cons -> Cons(xs.head, init(xs.tail))
                    }
                }

        fun <A> init2(xs: ListK<A>): ListK<A> {
            val buf = mutableListOf<A>()
            tailrec fun go(cur: ListK<A>): ListK<A> = when (cur) {
                Nil -> Nil
                is Cons -> if (cur.tail == Nil) ListK(buf.toList()) else {
                    buf += cur.head
                    go(cur.tail)
                }
            }
            return go(xs)
        }

        fun <A, B> foldRight(xs: ListK<A>, x: B, f: (A, B) -> B): B {
            val b = when (xs) {
                Nil -> x
                is Cons -> f(xs.head, foldRight(xs.tail, x, f))
            }
            println(b)
            return b
        }

        fun sumFoldRight(xs: ListK<Int>): Int =
                foldRight(xs, 0) { x, y -> x + y }

        fun productFoldRight(xs: ListK<Double>): Double =
                foldRight(xs, 1.0) { x, y -> x * y }

        fun <A> length(xs: ListK<A>): Int =
                foldRight(xs, 0) { _, acc -> acc + 1 }

        tailrec fun <A, B> foldLeft(xs: ListK<A>, x: B, f: (B, A) -> B): B =
                when (xs) {
                    Nil -> x
                    is Cons -> foldLeft(xs.tail, f(x, xs.head), f)
                }

        fun sumFoldLeft(xs: ListK<Int>): Int =
                foldLeft(xs, 0) { x, y -> x + y }

        fun productFoldLeft(xs: ListK<Double>): Double =
                foldLeft(xs, 1.0) { x, y -> x * y }

        fun <A> reverse(xs: ListK<A>): ListK<A> =
                foldLeft(xs, emptyListK()) { acc, h -> Cons(h, acc) }

        fun <A, B> foldRightViaFoldLetf(xs: ListK<A>, x: B, f: (A, B) -> B): B =
                foldLeft(reverse(xs), x) { b, a -> f(a, b) }

        fun <A, B> foldRightViaFoldLetfNotSafe(xs: ListK<A>, x: B, f: (A, B) -> B): B =
                foldLeft(xs, { b: B -> b }) { g, a -> { b -> g(f(a, b)) } }(x)

        fun <A, B> foldLeftViaFoldRight(xs: ListK<A>, x: B, f: (B, A) -> B): B =
                foldRight(xs, { b: B -> b }) { a, g -> { b -> g(f(b, a)) } }(x)

        fun <A> appendViaFoldRight(xs1: ListK<A>, xs2: ListK<A>): ListK<A> =
                foldRight(xs1, xs2) { x, y -> Cons(x, y) }

        fun <A> appendViaFoldLeft(xs1: ListK<A>, xs2: ListK<A>): ListK<A> =
                foldLeft(reverse(xs1), xs2) { x, y -> Cons(y, x) }

        fun <A> concat(xs: ListK<ListK<A>>): ListK<A> =
                foldRight(xs, emptyListK(), ::appendViaFoldRight)

        fun add1(xs: ListK<Int>): ListK<Int> =
                foldRight(xs, emptyListK()) { h, t -> Cons(h + 1, t) }

        fun doubleToString(xs: ListK<Double>): ListK<String> =
                foldRight(xs, emptyListK()) { h, t -> Cons(h.toString(), t) }

        fun <A, B> map(xs: ListK<A>, f: (A) -> B): ListK<B> =
                foldRight(xs, emptyListK()) { h, t -> Cons(f(h), t) }

        fun <A> filter(xs: ListK<A>, f: (A) -> Boolean): ListK<A> =
                foldRight(xs, emptyListK()) { h, t -> if (f(h)) Cons(h, t) else t }

        fun <A> filterViaFoldLeft(xs: ListK<A>, f: (A) -> Boolean): ListK<A> =
                foldRightViaFoldLetf(xs, emptyListK()) { h, t -> if (f(h)) Cons(h, t) else t }

        fun <A> filterLocalMutation(xs: ListK<A>, f: (A) -> Boolean): ListK<A> {
            val buf: MutableList<A> = mutableListOf()
            fun go(xs: ListK<A>): Unit =
                    when (xs) {
                        Nil -> Unit
                        is Cons -> {
                            if (f(xs.head)) buf += xs.head
                            go(xs.tail)
                        }
                    }
            go(xs)
            return ListK(buf)
        }

        fun <A, B> flatMap(xs: ListK<A>, f: (A) -> ListK<B>): ListK<B> =
                concat(map(xs, f))

        fun <A> filterViaFlatMap(xs: ListK<A>, f: (A) -> Boolean): ListK<A> =
                flatMap(xs) { x -> if (f(x)) ListK(x) else Nil }

        fun addPariwise(xs: ListK<Int>, zs: ListK<Int>): ListK<Int> {
            val pair = Pair(xs, zs)
            when(pair){
                pair.first -> pair.first.
                pair.second == Nil -> Nil
                else -> Cons(pair.first.)
            }
        }

    }
}

object Nil : ListK<Nothing>()
data class Cons<out A>(val head: A, val tail: ListK<A>) : ListK<A>()

fun main(args: Array<String>) {
    val ex1: ListK<Double> = Nil
    val ex2: ListK<Int> = Cons(1, Nil)
    val ex3: ListK<String> = Cons("a", Cons("b", Nil))

    val listK = ListK(1, 2, 3, 4)
    println(ListK.tail(listK))
    println(ListK.setHead(5, listK))
    println(listK)
    println(ListK.drop(listK, 2))
    println(dropWhile(listK, { x: Int -> x < 2 }))
    foldRight(ListK(1, 2, 3), Nil as ListK<Int>) { x, y -> Cons(x, y) }
    println(reverse(ListK(1, 2, 3)))
    println(appendViaFoldRight(ListK(1, 2, 3), ListK(4, 5, 6)))
    println(appendViaFoldLeft(ListK(1, 2, 3), ListK(4, 5, 6)))
    println("Concat: ${concat(ListK(ListK(4, 5, 6), ListK(7, 8, 9)))}")
    println("Add 1: ${add1(ListK(1, 2, 3))}")
    println("Filter mutation: ${filterLocalMutation(ListK(1, 2, 3)) { it < 2 }}")
    println("Filter flatMap: ${filterLocalMutation(ListK(1, 2, 3)) { it < 2 }}")
}