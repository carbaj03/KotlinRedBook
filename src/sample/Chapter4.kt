package sample

sealed class Option<out A> {

    companion object {
        fun <A> none(): Option<A> = None
     }

    abstract val isEmpty: Boolean

    internal val isDefined: Boolean = !isEmpty

    fun <B> map(f: (A) -> B): Option<B> =
            when (this) {
                is None -> none()
                is Some -> Some(f(get))
            }

    fun <B> getOrElse(default: () -> B): B =
            when (this) {
                is None -> default()
                is Some -> get as B
            }

    fun <B> flatmap(f: (A) -> Option<B>): Option<B> =
            map(f).getOrElse { none() }

    fun <B> flatMap_1(f: (A) -> Option<B>): Option<B> =
            when (this) {
                is None -> none()
                is Some -> f(get)
            }

    fun <B> orElse(ob: () -> Option<B>): Option<B> =
            this.map { Some(it) }.getOrElse(ob)

    fun <B> orElse_1(ob: () -> Option<B>): Option<B> =
            when (this) {
                is None -> ob()
                else -> this as Option<B>
            }


    fun filter(f: (A) -> Boolean): Option<A> =
            when (this) {
                is Some -> if (f(get)) this else none()
                else -> none()
            }

    fun filter_1(f: (A) -> Boolean): Option<A> =
            this.flatmap { if (f(it)) Some(it) else None }

    data class Some<out A>(val get: A) : Option<A>(){
        override val isEmpty = false
    }

    object None : Option<Nothing>(){
        override val isEmpty = true
    }
}

fun mean(xs: List<Double>): Option<Double> =
        if (xs.isEmpty()) Option.None
        else Option.Some(xs.sum() / xs.size)

fun variance(xs: List<Double>): Option<Double> =
        mean(xs).flatmap { m -> mean(xs.map { Math.pow(it - m, 2.0) }) }

fun <A, B, C> map2(a: Option<A>, b: Option<B>, f: (A, B) -> C): Option<C> =
        a.flatmap { aa -> b.map { bb -> f(aa, bb) } }

fun <A> sequence(a: ListK<Option<A>>): Option<ListK<A>> =
        when (a) {
            ListK.Nil -> Option.Some(ListK.Nil)
            is ListK.Cons -> sequence(a).map { ListK.init(it) }
        }

fun <A, B> traverse(a: ListK<A>, f: (A) -> Option<B>): Option<ListK<B>> =
        when (a) {
            is ListK.Nil -> Option.Some(ListK.Nil)
            is ListK.Cons -> map2(f(a.head), traverse(a.tail, f), { h, t -> ListK.Cons(h, t) })
        }

fun <A, B> traverse_1(a: ListK<A>, f: (A) -> Option<B>): Option<ListK<B>> =
        ListK.foldRight<A, Option<ListK<B>>>(a, Option.Some(ListK.Nil), { h, t -> map2(f(h), t, { hh, tt -> ListK.Cons(hh, tt) }) })

fun <A> sequenceViaTraverse(a: ListK<Option<A>>): Option<ListK<A>> =
        traverse(a, { it })











sealed class Either<out E, out A> {
    fun <B> map(f: (A) -> B): Either<E, B> =
            when (this) {
                is Left -> Left(value)
                is Right -> Right(f(value))
            }

    fun <EE, B> flatMap(f: (A) -> Either<EE, B>): Either<EE, B> =
            when (this) {
                is Left -> Left(value) as Either<EE, B>
                is Right -> f(value)
            }

    fun <EE, AA> orElse(b: () -> Either<EE, AA>): Either<EE, AA> =
            when (this) {
                is Left -> b()
                is Right -> Right(value) as Either<EE, AA>
            }

    fun <EE, B, C> map2(b: Either<EE, B>, f: (A, B) -> C): Either<EE, C> =
            b.flatMap { bb -> this.map { aa -> f(aa, bb) } } as Either<EE, C>

    fun <E, A, B> traverse(es: ListK<A>, f: (A) -> Either<E, B>): Either<E, ListK<B>> =
            when (es) {
                is ListK.Nil -> Right(ListK.Nil)
                is ListK.Cons -> f(es.head).map2(traverse(es.tail, f), { h, t -> ListK.Cons(h, t) })
            }

    fun <E, A, B> traverse_1(es: ListK<A>, f: (A) -> Either<E, B>): Either<E, ListK<B>> =
            ListK.foldRight<A, Either<E, ListK<B>>>(es, Right(ListK.Nil), { h, t -> f(h).map2(t, { hh, tt -> ListK.Cons(hh, tt) }) })

    fun <E, A> sequence(es: ListK<Either<E, A>>): Either<E, ListK<A>> =
            traverse(es, { it })
}

data class Left<out E>(val value: E) : Either<E, Nothing>()
data class Right<out A>(val value: A) : Either<Nothing, A>()





//For acumulate errors
sealed class Partial<out E, out A>{
    fun <B> map(f: (A) -> B): Partial<E, B> =
            when (this) {
                is Errors -> Errors(e)
                is Success -> Success(f(a))
            }

    fun <EE, B> flatMap(f: (A) -> Partial<EE, B>): Partial<EE, B> =
            when (this) {
                is Errors -> Errors(e) as Partial<EE, B>
                is Success-> f(a)
            }

    fun <EE, AA> orElse(b: () -> Partial<EE, AA>): Partial<EE, AA> =
            when (this) {
                is Errors -> b()
                is Success -> Right(a) as Partial<EE, AA>
            }

    fun <EE, B, C> map2(b: Partial<EE, B>, f: (A, B) -> C): Partial<EE, C> =
            b.flatMap { bb -> this.map { aa -> f(aa, bb) } } as Partial<EE, C>

    fun <E, A, B> traverse(es: ListK<A>, f: (A) -> Partial<E, B>): Partial<E, ListK<B>> =
            when (es) {
                is ListK.Nil -> Success(ListK.Nil)
                is ListK.Cons -> f(es.head).map2(traverse(es.tail, f), { h, t -> ListK.Cons(h, t) })
            }

    fun <E, A, B> traverse_1(es: ListK<A>, f: (A) -> Partial<E, B>): Partial<E, ListK<B>> =
            ListK.foldRight<A, Partial<E, ListK<B>>>(es, Success(ListK.Nil), { h, t -> f(h).map2(t, { hh, tt -> ListK.Cons(hh, tt) }) })

    fun <E, A> sequence(es: ListK<Partial<E, A>>): Partial<E, ListK<A>> =
            traverse(es, { it })
}

data class Errors<out E>(val e: ListK<E>) : Partial<E, Nothing>()
data class Success<out A>(val a: A) : Partial<Nothing, A>()




fun main(args: Array<String>) {
    println(mkPerson("", -2))
}

data class Person(val name: Name, val age: Age)

data class Name(val name: String)

data class Age(val age: Int)

fun mkName(name: String): Partial<String, Name> =

        if (name == "" || name == null) Errors(ListK.apply("Name is empty."))
        else Success(Name(name))

fun mkAge(age: Int): Partial<String, Age> =
        if (age < 0) Errors(ListK.apply("Bad age"))
        else Success(Age(age))

fun mkPerson(name: String, age: Int): Partial<String, Person> =
        mkName(name).map2(mkAge(age), ::Person)

