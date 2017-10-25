package sample

import java.util.concurrent.TimeUnit


interface ExecutorService {
    fun <A> submit(a: Callable<A>): Future<A>
}

interface Callable<out A> {
    fun call(): A
}

interface Future<A> {
    fun get(): A
    fun get(timeout: Long, unit: TimeUnit): A
    fun cancel(evenIfRunning: Boolean): Boolean
    fun isDone(): Boolean
    fun isCancelled(): Boolean
}

typealias Par<A> = (ExecutorService) -> Future<A>
fun <A> run(s: ExecutorService, a: Par<A>): Future<A> = a(s)

object ParK {
    fun <A> unit(a: A): Par<A> = { es: ExecutorService -> UnitFuture(a) }

    class UnitFuture<A>(private val get: A) : Future<A> {
        override fun get(): A = get
        override fun isDone() = true
        override fun get(timeout: Long, units: TimeUnit) = get
        override fun isCancelled() = false
        override fun cancel(evenIfRunning: Boolean): Boolean = false
    }

    fun <A, B, C> map2(a: Par<A>, b: Par<B>, f: (A, B) -> C): Par<C> = {
        val (af, bf) = Pair(a(it), b(it))
        Map2Future(af, bf, f)
    }

    class Map2Future<A, B, C>(val a: Future<A>, val b: Future<B>, val f: (A, B) -> C) : Future<C> {
        @Volatile private var cache: Option<C> = Option.None

        override fun isDone() =
                cache.isDefined

        override fun isCancelled() =
                a.isCancelled() || b.isCancelled()

        override fun cancel(evenIfRunning: Boolean) =
                a.cancel(evenIfRunning) || b.cancel(evenIfRunning)

        override fun get() =
                compute(Long.MAX_VALUE)

        override fun get(timeout: Long, units: TimeUnit): C =
                compute(TimeUnit.NANOSECONDS.convert(timeout, units))

        private fun compute(timeoutInNanos: Long): C = when (cache) {
            is Option.Some -> (cache as Option.Some<C>).get
            is Option.None -> {
                val start = System.nanoTime()
                val ar = a.get(timeoutInNanos, TimeUnit.NANOSECONDS)
                val stop = System.nanoTime()
                val aTime = stop - start
                val br = b.get(timeoutInNanos - aTime, TimeUnit.NANOSECONDS)
                val ret = f(ar, br)
                cache = Option.Some(ret)
                ret
            }
        }

        fun <A> fork(a: Par<A>): Par<A> = {
            it.submit(object : Callable<A> {
                override fun call() = a(it).get()
            })
        }

        fun <A> lazyUnit(a: A): Par<A> =
                fork(unit(a))

        fun <A, B> asyncF(f: (A) -> B): (A) -> Par<B> =
                { lazyUnit(f(it)) }

        fun sortPar(parList: Par<List<Int>>): Par<List<Int>> =
                map2(parList, unit({}), { a, _ -> a.sorted() })

        fun <A, B> map(pa: Par<A>, f: (A) -> B): Par<B> =
                map2(pa, unit({}), { a, _ -> f(a) })

        fun sortParMap(parList: Par<List<Int>>) =
                map(parList, { it.sorted() })

        fun <A> sequence_simple(l: ListK<Par<A>>): Par<ListK<A>> =
                ListK.foldRight(l, (unit(ListK.empty())), { h, t -> map2(h, t, { a1, b1 -> ListK.setHead(b1, a1) }) })

        fun <A> sequenceRight(l: ListK<Par<A>>): Par<ListK<A>> =
                when (l) {
                    is ListK.Nil -> unit(ListK.empty())
                    is ListK.Cons -> map2(l.head, fork(sequenceRight(l.tail)), { a1, b1 -> ListK.setHead(b1, a1) })
                }

        fun <A, B> parMap(ps: ListK<A>, f: (A) -> B): Par<ListK<B>> =
                fork(a(ps, f))

        fun <A, B> a(ps: ListK<A>, f: (A) -> B): Par<ListK<B>> {
            val fbs: ListK<Par<B>> = ListK.map(ps, asyncF(f))
            return sequence_simple(fbs)
        }

    }

}