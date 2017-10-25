package sample

interface RNG {
    fun nextInt(): Pair<Int, RNG>
}

data class SimpleRNG(val seed: Long) : RNG {
    override fun nextInt(): Pair<Int, RNG> {
        val newSeed = (seed * 0x5DEECE66DL + 0xBL) and 0xFFFFFFFFFFFFL
        val nextRNG = SimpleRNG(newSeed)
        val n = (newSeed ushr 16).toInt()
        return Pair(n, nextRNG)
    }
}

fun nonNegativeInt(rng: RNG): Pair<Int, RNG> {
    val (i, r) = rng.nextInt()
    return Pair(if (i < 0) -(i + 1) else i, r)
}

fun double(rng: RNG): Pair<Double, RNG> {
    val (i, r) = nonNegativeInt(rng)
    return Pair(i / (Int.MAX_VALUE.toDouble() + 1), r)
}

fun intDouble(rng: RNG): Pair<Pair<Int, Double>, RNG> {
    val (int, rng1) = rng.nextInt()
    val (double, rng2) = double(rng1)
    return Pair(Pair(int, double), rng2)
}

fun doubleInt(rng: RNG): Pair<Pair<Double, Int>, RNG> {
    val (i, r1) = intDouble(rng)
    return Pair(Pair(i.second, i.first), r1)
}

fun double3(rng: RNG): Pair<Pair<Pair<Double, Double>, Double>, RNG> {
    val (d1, r1) = double(rng)
    val (d2, r2) = double(r1)
    val (d3, r3) = double(r2)
    return Pair(Pair(Pair(d1, d2), d3), r3)
}

fun ints(count: Int, rng: RNG): Pair<ListK<Int>, RNG> =
        if (count <= 0)
            Pair(ListK.Nil, rng)
        else {
            val (x, r1) = rng.nextInt()
            val (xs, r2) = ints(count - 1, r1)
            Pair(ListK.Cons(x, xs), r2)
        }

// A tail-recursive solution
fun ints2(count: Int, rng: RNG): Pair<ListK<Int>, RNG> {
    fun go(count: Int, r: RNG, xs: ListK<Int>): Pair<ListK<Int>, RNG> =
            if (count <= 0)
                Pair(xs, r)
            else {
                val (x, r2) = r.nextInt()
                go(count - 1, r2, ListK.Cons(x, xs))
            }
    return go(count, rng, ListK.Nil)
}

typealias Rand<A> = (RNG) -> Pair<A, RNG>

fun <A, B> map(s: Rand<A>, f: (A) -> B): Rand<B> = {
    val (a, rng2) = s(it)
    Pair(f(a), rng2)
}

fun nonNegativeEven(): Rand<Int> =
        map(::nonNegativeInt, { i -> i - i % 2 })

fun double(): Rand<Double> =
        map(::nonNegativeInt, { it / ((Int.MAX_VALUE.toDouble() + 1)) })

fun <A, B, C> map2(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> = {
    val (a, r1) = ra(it)
    val (b, r2) = rb(r1)
    Pair(f(a, b), r2)
}

fun <A, B> both(ra: Rand<A>, rb: Rand<B>): Rand<Pair<A, B>> =
        map2(ra, rb, { x, y -> Pair(x, y) })

fun <A> unit(a: A): Rand<A> = { Pair(a, it) }

fun <A> sequenceRand(fs: ListK<Rand<A>>): Rand<ListK<A>> =
        ListK.foldRight(fs, unit(ListK.empty()), { f, acc -> map2(f, acc, { x, y -> ListK.Cons(x, y) }) })

//fun ints(count: Int): Rand<ListK<Int>> =
//    sequenceRand(constant(1).toList())

fun nonNegativeLessThan(n: Int): Rand<Int> =
        map(::nonNegativeInt) { it % n }

fun <A, B> flatMap(f: Rand<A>, g: (A) -> Rand<B>): Rand<B> = {
    val (a, r1) = f(it)
    g(a)(r1) // We pass the new state along
}

fun nonNegativeLessThanF(n: Int): Rand<Int> =
        flatMap(::nonNegativeInt) {
            val mod = it % n
            if (it + (n - 1) - mod >= 0) unit(mod) else nonNegativeLessThanF(n)
        }

fun <A, B> mapF(s: Rand<A>, f: (A) -> B): Rand<B> =
        flatMap(s, { unit(f(it)) })

fun <A, B, C> map2F(ra: Rand<A>, rb: Rand<B>, f: (A, B) -> C): Rand<C> =
        flatMap(ra, { a -> map(rb, { b -> f(a, b) }) })



fun main(args: Array<String>) {
    print("\nnextInt: ${SimpleRNG(-1).nextInt()}")
    print("\nnonNegative: ${nonNegativeInt(SimpleRNG(-1))}")
    print("\ndouble: ${double(SimpleRNG(-1))}")
    print("\nintDouble: ${intDouble(SimpleRNG(-1))}")
    print("\ndoubleInt: ${doubleInt(SimpleRNG(-1))}")
    print("\ndouble3: ${double3(SimpleRNG(-1))}")
    print("\nints: ${ints(5, SimpleRNG(-1))}")
    print("\nints recursive: ${ints2(5, SimpleRNG(-1))}")
    print("\nnonNegativeEven: ${nonNegativeEven()}")
    print("\ndouble: ${double()}")
    print("\nboth: ${double()}")
    print("\nnonNegativeInt: ${nonNegativeLessThan(3)}")
}


data class State<S, out A>(run: (S) -> Pair<A, S>) {

}
