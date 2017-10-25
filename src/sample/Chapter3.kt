package sample

sealed class ListK<out A> {

    companion object {

        fun <A>empty():ListK<A>  = Nil

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

        fun <A> apply(vararg ars: A): ListK<A> =
                if (ars.isEmpty()) Nil
                else Cons(ars[0], apply(ars.drop(1)))

        fun <A> apply(ars: List<A>): ListK<A> =
                if (ars.isEmpty()) Nil
                else Cons(ars[0], apply(ars.drop(1)))


        fun <A> tail(xs: ListK<A>): ListK<A> = when (xs) {
            Nil -> Nil
            is Cons -> xs.tail
        }

        fun <A> setHead(xs: ListK<A>, h: A): ListK<A> = when (xs) {
            Nil -> Nil
            is Cons -> Cons(h, xs)
        }

        fun <A> drop(l: ListK<A>, n: Int): ListK<A> =
                if (n <= 0) l
                else when (l) {
                    Nil -> Nil
                    is Cons -> drop(l.tail, n - 1)
                }

        fun <A> dropWhile(l: ListK<A>, f: (A) -> Boolean): ListK<A> =
                when (l) {
                    is Cons -> if (f(l.head)) dropWhile(l.tail, f) else l
                    else -> l
                }

        fun <A> init(l: ListK<A>): ListK<A> =
                when (l) {
                    Nil -> Nil
                    is Cons -> if (l.tail == Nil) Nil else Cons(l.head, init(l.tail))
                }

        fun <A> init2(l: ListK<A>): ListK<A> {
            val buf = mutableListOf<A>()
            tailrec fun go(cur: ListK<A>): ListK<A> = when (cur) {
                Nil -> Nil
                is Cons -> if (cur.tail == Nil) ListK.apply(buf.toList()) else {
                    buf += cur.head
                    go(cur.tail)
                }
            }
            return go(l)
        }

        fun <A, B> foldRight(xs: ListK<A>, z: B, f: (A, B) -> B): B =
                when (xs) {
                    Nil -> z
                    is Cons -> f(xs.head, foldRight(xs.tail, z, f))
                }

        fun sumR(xs: ListK<Int>): Int =
                foldRight(xs, 0, { x, y -> x + y })

        fun productR(xs: ListK<Double>): Double =
                foldRight(xs, 1.0, { x, y -> x * y })

        fun <A> lengthR(l: ListK<A>): Int =
                foldRight(l, 0, { _, acc -> acc + 1 })

        tailrec fun <A, B> foldLeft(l: ListK<A>, z: B, f: (B, A) -> B): B =
                when (l) {
                    Nil -> z
                    is Cons -> foldLeft(l.tail, f(z, l.head), f)
                }

        fun sumL(xs: ListK<Int>): Int =
                foldLeft(xs, 0, { x, y -> x + y })

        fun productL(xs: ListK<Double>): Double =
                foldLeft(xs, 1.0, { x, y -> x * y })

        fun <A> lengthL(l: ListK<A>): Int =
                foldLeft(l, 0, { acc, _ -> acc + 1 })

        fun <A> reverse(l: ListK<A>): ListK<A> =
                foldLeft(l, ListK.apply(), { acc, h -> Cons(h, acc) })

        fun <A, B> foldRightL(l: ListK<A>, z: B, f: (A, B) -> B): B =
                foldLeft(reverse(l), z, { b, a -> f(a, b) })

        fun <A, B> foldRightL2(l: ListK<A>, z: B, f: (A, B) -> B): B =
                foldLeft(l, { b: B -> b }, { g, a -> { b -> g(f(a, b)) } })(z)

        fun <A> appendR(l: ListK<A>, r: ListK<A>): ListK<A> =
                foldRight(l, r, { x, y -> Cons(x, y) })

        fun <A> concat(l: ListK<ListK<A>>): ListK<A> =
                foldLeft(l, ListK.apply(), { x, y -> appendR(x, y) })

        fun mapInt(l: ListK<Int>): ListK<Int> =
                foldRightL(l, ListK.apply(), { h, t -> Cons(h + 1, t) })

        fun mapDoubleString(l: ListK<Double>): ListK<String> =
                foldRightL(l, ListK.apply(), { h, t -> Cons(h.toString(), t) })

        fun <A, B> map(l: ListK<A>, f: (A) -> B): ListK<B> =
                foldRightL(l, ListK.apply(), { h, t -> Cons(f(h), t) })

        fun <A, B> mapL(l: ListK<A>, f: (A) -> B): ListK<B> =
                foldRightL(l, ListK.apply(), { h, t -> Cons(f(h), t) })

        fun <A, B> mapM(l: ListK<A>, f: (A) -> B): ListK<B> {
            val buf = mutableListOf<B>()

            fun go(l: ListK<A>): Unit = when (l) {
                Nil -> Unit
                is Cons -> {
                    buf += f(l.head)
                    go(l.tail)
                }
            }

            go(l)
            return ListK.apply(buf)
        }

        fun <A> filter(l: ListK<A>, f: (A) -> Boolean): ListK<A> =
                foldRightL(l, ListK.apply(), { h, t -> if (f(h)) Cons(h, t) else t })

        fun <A, B> flatMap(l: ListK<A>, f: (A) -> ListK<B>): ListK<B> =
                concat(mapM(l, f))

        fun <A> filterFM(l: ListK<A>, f: (A) -> Boolean): ListK<A> =
                flatMap(l, { h -> if (f(h)) ListK.apply(h) else Nil })

        fun zipInt(l: ListK<Int>, r: ListK<Int>): ListK<Int> =
                when (l) {
                    Nil -> Nil
                    is Cons -> when (r) {
                        Nil -> Nil
                        is Cons -> Cons(l.head + r.head, zipInt(l.tail, r.tail))
                    }
                }

        fun <A, B, C> zip(l: ListK<A>, r: ListK<B>, f: (A, B) -> C): ListK<C> =
                when (l) {
                    Nil -> Nil
                    is Cons -> when (r) {
                        Nil -> Nil
                        is Cons -> Cons(f(l.head, r.head), zip(l.tail, r.tail, f))
                    }
                }

        tailrec fun <A> startsWith(l: ListK<A>, prefix: ListK<A>): Boolean =
                when (prefix) {
                    Nil -> true
                    is Cons -> when (l) {
                        is Cons -> if (l.head == prefix.head) startsWith(l.tail, prefix.tail) else false
                        else -> false
                    }
                }

        fun <A> hasSubsequence(sup: ListK<A>, sub: ListK<A>): Boolean =
                when (sup) {
                    Nil -> sub == Nil
                    is Cons -> if (startsWith(sup, sub)) true else hasSubsequence(sup.tail, sub)
                }


    }

    object Nil : ListK<Nothing>()

    data class Cons<out A>(val head: A, val tail: ListK<A>) : ListK<A>()
}

sealed class Tree<A> {
    companion object {
        fun <A> size(t: Tree<A>): Int =
                when (t) {
                    is Leaf -> 1
                    is Branch -> 1 + size(t.left) + size(t.right)
                }

        fun maximun(t: Tree<Int>): Int =
                when (t) {
                    is Leaf -> t.value
                    is Branch -> maximun(t.left) max maximun(t.right)
                }

        fun <A> deph(t: Tree<A>): Int =
                when (t) {
                    is Leaf -> 0
                    is Branch -> 1 + (deph(t.left) max deph(t.right))
                }

        fun <A, B> map(t: Tree<A>, f: (A) -> B): Tree<B> =
                when (t) {
                    is Leaf -> Leaf(f(t.value))
                    is Branch -> Branch(map(t.left, f), map(t.right, f))
                }

        fun <A, B> fold(t: Tree<A>, f: (A) -> B, g: (B, B) -> B): B =
                when (t) {
                    is Leaf -> f(t.value)
                    is Branch -> g(fold(t.left, f, g), fold(t.right, f, g))
                }

        fun <A> sizeF(t: Tree<A>): Int =
                fold(t, { _ -> 1 }, { l, r -> 1 + l + r })

        fun maximunF(t: Tree<Int>): Int =
                fold(t, { it }, { l, r -> l max r })

        fun <A> dephF(t: Tree<A>): Int =
                fold(t, { 0 }, { l, r -> 1 + (l max r) })

    }

    data class Leaf<A>(val value: A) : Tree<A>()
    data class Branch<A>(val left: Tree<A>, val right: Tree<A>) : Tree<A>()
}

infix fun Int.max(a: Int): Int =
        Math.max(this, a)

fun main(args: Array<String>) {
    val x = when (ListK.apply(1, 2, 3, 4, 5)) {
        ListK.Nil -> 42
        ListK.Cons(2, ListK.apply(2, 3, 4, 5)) -> 22
        else -> 101
    }


    println(ListK.sum(ListK.apply((1..2).toList())))
    println(ListK.product(ListK.apply(listOf(100.1, 20.2))))
    println(x)

    println(ListK.setHead(ListK.apply((0..9).toList()), 2))
    println(ListK.tail(ListK.apply((0..9).toList())))
    println(ListK.drop(ListK.apply((0..9).toList()), 2))
    println(ListK.dropWhile(ListK.apply((0..9).toList()), { it <= 2 }))

    println(ListK.init(ListK.apply((0..6).toList())))
    println(ListK.init2(ListK.apply((0..6).toList())))

    println(ListK.sumR(ListK.apply((1..2).toList())))
    println(ListK.productR(ListK.apply(listOf(100.1, 20.2))))
    println(ListK.lengthR(ListK.apply((0..6).toList())))

    println(ListK.sumL(ListK.apply((1..2).toList())))
    println(ListK.productL(ListK.apply(listOf(100.1, 20.2))))
    println(ListK.lengthL(ListK.apply((0..6).toList())))

    println(ListK.reverse(ListK.apply((0..6).toList())))
    println(ListK.appendR(ListK.apply((0..6).toList()), ListK.apply((0..6).toList())))

    println(ListK.concat(ListK.apply(ListK.apply((1..2).toList()), ListK.apply((1..2).toList()))))
    println("mapInt: " + ListK.mapInt(ListK.apply((0..6).toList())))
    println("mapDoubleString: " + ListK.mapDoubleString(ListK.apply(2.2, 1.1)))
    println("map: " + ListK.map(ListK.apply(2.2, 1.1), { it + 1 }))
    println("mapM: " + ListK.mapM(ListK.apply(2.2, 1.1), { it + 1 }))
    println("filter: " + ListK.filter(ListK.apply(2.2, 1.1), { it > 2 }))
    println("flatMap: " + ListK.flatMap(ListK.apply(2.2, 1.1), { ListK.apply(it, it) }))
    println("filterFM: " + ListK.filterFM(ListK.apply(2.2, 1.1), { it > 2 }))
    println("zipInt: " + ListK.zipInt(ListK.apply(2, 1), ListK.apply(2, 1)))
    println("zip: " + ListK.zip(ListK.apply(2, 1), ListK.apply(2, 1), { l, r -> l + r }))
    println("subsecuence: " + ListK.hasSubsequence(ListK.apply(2, 1), ListK.apply(1, 2)))

    val tree = Tree.Branch(Tree.Branch(Tree.Leaf(2), Tree.Leaf(1)), Tree.Branch(Tree.Branch(Tree.Leaf(100), Tree.Leaf(0)), Tree.Leaf(2)))
    println("size: " + Tree.size(tree))
    println("max: " + Tree.maximun(tree))
    println("deph: " + Tree.deph(tree))
    println("map: " + Tree.map(tree, { it.toFloat() }))
    println("map: " + Tree.map(tree, { it.toFloat() }))
    println("sizeF: " + Tree.sizeF(tree))
    println("maxF: " + Tree.maximunF(tree))
    println("dephF: " + Tree.dephF(tree))

}