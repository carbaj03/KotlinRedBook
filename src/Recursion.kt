fun factorial(n: Int): Int {
    tailrec fun go(n: Int, acc: Int): Int =
            if (n <= 0) acc
            else go(n - 1, n * acc)
    return go(n, 1)
}

fun factorial2(n: Int): Int {
    if (n <= 0) {
        return 1
    }
    var fact = 1
    (1..n).forEach{ fact *= it }
    return fact
}


fun main(args: Array<String>) =
        println(factorial(6))