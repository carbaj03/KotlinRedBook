package sample

import sample.StreamK.Companion.cons
import sample.StreamK.Companion.empty


sealed class StreamK<out A> {
    companion object {
        fun <A> cons(hd: () -> A, tl: () -> StreamK<A>): StreamK<A> {
            val head: () -> A by lazy { hd }
            val tail: () -> StreamK<A> by lazy { tl }
            return Cons(head, tail)
        }

        fun <A> empty(): StreamK<A> = Empty

        fun <A> apply(vararg s: A): StreamK<A> =
                if (s.isEmpty()) empty()
                else cons({ s.first() }, { apply(s.drop(1)) })

        fun <A> apply(ars: List<A>): StreamK<A> =
                if (ars.isEmpty()) empty()
                else cons({ ars[0] }, { apply(ars.drop(1)) })

    }

    object Empty : StreamK<Nothing>()

    data class Cons<out A>(val h: () -> A, val t: () -> StreamK<A>) : StreamK<A>()
}

fun <B, A> StreamK<A>.foldRight(z: () -> B, f: (A, () -> B) -> B): B = when (this) {
    StreamK.Empty -> z()
    is StreamK.Cons -> f(h(), { t().foldRight(z, f) })
}

fun <A> StreamK<A>.headOption(): Option<A> =
        foldRight({ Option.none() }, { h, _ -> Option.Some(h) })

fun <B, A> StreamK<A>.map(p: (A) -> B): StreamK<B> =
        foldRight({ empty() }, { h, t -> cons({ p(h) }, t) })

fun <A> StreamK<A>.existsR(p: (A) -> Boolean): Boolean =
        foldRight({ false }, { h, t -> p(h) || t() })

fun <A> StreamK<A>.takeWhile(f: (A) -> Boolean): StreamK<A> = when (this) {
    is StreamK.Cons -> if (f(h())) cons({ h() }, { t().takeWhile(f) }) else empty()
    is StreamK.Empty -> empty()
}

tailrec fun <A> StreamK<A>.drop(n: Int): StreamK<A> = when (this) {
    is StreamK.Cons -> when {
        n > 0 -> t().drop(n - 1)
        else -> this
    }
    else -> this
}

fun <A> StreamK<A>.forAll(p: (A) -> Boolean): Boolean =
        foldRight({ true }, { h, t -> p(h) && t() })


fun <A> StreamK<A>.takeWhileR(f: (A) -> Boolean): StreamK<A> =
        foldRight({ empty() }, { h, t -> if (f(h)) cons({ h }, t) else empty() })

fun <A> StreamK<A>.toListRecursive(): ListK<A> = when (this) {
    is StreamK.Cons -> ListK.setHead(t().toListRecursive(), h())
    else -> ListK.Nil
}

fun <A> StreamK<A>.toList(): ListK<A> {
    tailrec fun go(s: StreamK<A>, acc: ListK<A>): ListK<A> = when (s) {
        is StreamK.Cons -> go(s.t(), ListK.setHead(acc, s.h()))
        else -> acc
    }
    return ListK.reverse(go(this, ListK.Nil))
}

fun <A> StreamK<A>.toListFast(): ListK<A> {
    val buf = mutableListOf<A>()
    tailrec fun go(s: StreamK<A>): ListK<A> = when (s) {
        is StreamK.Cons -> {
            buf += s.h()
            go(s.t())
        }
        else -> ListK.apply(buf.toList())
    }
    return go(this)
}

fun <A> StreamK<A>.take(n: Int): StreamK<A> = when (this) {
    is StreamK.Cons ->
        when (t()) {
            is StreamK.Cons -> when {
                n > 1 -> cons({ h() }, { t().take(n - 1) })
                n == 1 -> cons({ h() }, { empty() })
                else -> empty()
            }
            else -> empty()
        }
    else -> empty()
}

fun <A> StreamK<A>.exists(p: (A) -> Boolean): Boolean = when (this) {
    is StreamK.Cons -> p(h()) || t().exists(p)
    else -> false
}

fun <A> StreamK<A>.filter(f: (A) -> Boolean): StreamK<A> =
        foldRight({ empty() }, { h, t -> if (f(h)) cons({ h }, t) else t() })

fun <A> StreamK<A>.append(s: () -> StreamK<A>): StreamK<A> =
        foldRight(s, { h, t -> cons({ h }, t) })

fun <A, B> StreamK<A>.flatMap(f: (A) -> StreamK<B>): StreamK<B> =
        foldRight({ empty() }, { h, t -> f(h).append(t) })

fun <A> StreamK<A>.find(p: (A) -> Boolean): Option<A> =
        filter(p).headOption()

fun ones(): StreamK<Int> =
        StreamK.cons({ 1 }, { ones() })

fun <A> constant(a: A): StreamK<A> {
    val tail: StreamK<A> by lazy { StreamK.Cons({ a }, { constant(a) }) }
    return tail
}

fun from(n: Int): StreamK<Int> =
        cons({ n }, { from(n + 1) })

fun fibs(): StreamK<Int> {
    fun go(f0: Int, f1: Int): StreamK<Int> =
            cons({ f0 }, { go(f1, f0 + f1) })
    return go(0, 1)
}

fun <S, A> unfold(z: S, f: (S) -> Option<Pair<A, S>>): StreamK<A> = with(f(z)) {
    when (this) {
        is Option.None -> StreamK.Empty
        is Option.Some -> cons({ get.first }, { unfold(get.second, f) })
    }
}

fun onesU(): StreamK<Int> =
        unfold(1, { Option.Some(Pair(1, 1)) })

fun fibsU(): StreamK<Int> =
        unfold(Pair(0, 1), { x -> Option.Some(Pair(x.first, Pair(x.second, x.first + x.second))) })

fun <A> constantU(a: A): StreamK<A> =
        unfold(a, { Option.Some(Pair(a, a)) })

fun fromU(n: Int): StreamK<Int> =
        unfold(n, { m -> Option.Some(Pair(m, m + 1)) })

fun <A, B> StreamK<A>.mapU(f: (A) -> B): StreamK<B> =
        unfold(this, { x ->
            when (x) {
                is StreamK.Empty -> Option.None
                is StreamK.Cons -> Option.Some(Pair(f(x.h()), x.t()))
            }
        })

fun <A> StreamK<A>.takeU(n: Int): StreamK<A> =
        unfold(Pair(this, n), {
            val first = it.first
            val second = it.second
            when (first) {
                is StreamK.Empty -> Option.None
                is StreamK.Cons ->
                    when {
                        second == 1 -> Option.Some(Pair(first.h(), Pair(empty(), 0)))
                        second > 1 -> Option.Some(Pair(first.h(), Pair(first.t(), second - 1)))
                        else -> Option.None
                    }

            }
        })

fun <A> StreamK<A>.takeWhileU(f: (A) -> Boolean): StreamK<A> =
        unfold(this, {
            when (it) {
                is StreamK.Cons -> if (f(it.h())) Option.Some(Pair(it.h(), it.t())) else Option.None
                is StreamK.Empty -> Option.None
            }
        })

fun <A, B, C> StreamK<A>.zipWith(s: StreamK<B>, f: (A, B) -> C): StreamK<C> =
        unfold(Pair(this, s), {
            val first = it.first
            val second = it.second
            when (first) {
                is StreamK.Cons -> when (second) {
                    is StreamK.Cons -> Option.Some(Pair(f(first.h(), second.h()), Pair(first.t(), second.t())))
                    is StreamK.Empty -> Option.None
                }
                is StreamK.Empty -> Option.None
            }
        })

fun <A, B, C> StreamK<A>.zipWithAll(s2: StreamK<B>, f: (Option<A>, Option<B>) -> C): StreamK<C> =
        unfold(Pair(this, s2), {
            val first = it.first
            val second = it.second
            when (first) {
                is StreamK.Empty -> when (second) {
                    is StreamK.Empty -> Option.None
                    is StreamK.Cons -> Option.Some(Pair((f(Option.none(), Option.Some(second.h()))), Pair(empty<A>(), second.t())))
                }
                is StreamK.Cons -> when (second) {
                    is StreamK.Empty -> Option.Some(Pair((f(Option.Some(first.h()), Option.none())), Pair(first.t(), empty())))
                    is StreamK.Cons -> Option.Some(Pair((f(Option.Some(first.h()), Option.Some(second.h()))), Pair(first.t(), second.t())))
                }
            }
        })

fun <A, B> StreamK<A>.zip(s2: StreamK<B>): StreamK<Pair<A, B>> =
        zipWith(s2, { x, y -> Pair(x, y) })

fun main(args: Array<String>) {
    print("\nones: ")
    ones().take(40).map { print("$it ") }.toList()
    print("\nconstant: ")
    constant(2).take(100).mapU { print("$it ") }.toList()
    print("\nfrom: ")
    from(1).take(100).mapU { print("$it ") }.toList()
    print("\nfibs: ")
    fibs().take(10).mapU { print("$it ") }.toList()
    print("\nunfold ones: ")
    onesU().take(10).mapU { print("$it ") }.toList()
    print("\nunfold fibs: ")
    fibsU().takeU(10).mapU { print("$it ") }.toList()
    print("\nunfold constant: ")
    constantU(5).takeU(50).mapU { print("$it ") }.toList()
    print("\nunfold from: ")
    fromU(1).takeU(20).mapU { print("$it ") }.toList()
    print("\nunfold takeWhile: ")
    fromU(1).takeWhileU { it < 20 }.mapU { print("$it ") }.toList()
    print("\nunfold zipWith: ")
    fromU(1).takeU(2).zipWith(fromU(1).take(5), { x, y -> x + y }).mapU { print("$it ") }.toList()
    print("\nunfold zip: ")
    fromU(1).takeU(2).zip(fromU(1).take(5)).mapU { print("$it ") }.toList()
    print("\nunfold zipAll: ")
    fromU(1).takeU(2).zipWithAll(fromU(1).take(5), {x,y -> x.map { a -> y.map { a + it} } }).mapU { print("$it ") }.toList()
}


