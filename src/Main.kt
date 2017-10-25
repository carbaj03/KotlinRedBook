// A comment!
/* Another comment */
/** A documentation comment */
object MyObject {
    fun abs(n: Int): Int =
        if (n < 0) -n
        else n

    fun factorial(n: Int): Int {
        tailrec fun go(n: Int, acc: Int): Int =
                if (n <= 0) acc
                else go(n - 1, n * acc)
        return go(n, 1)
    }

    fun formatAbs(x: Int): String {
        val msg = "The absolute value of %d is %d"
        return msg.format(x, abs(x))
    }

    fun formatFactorial(x: Int): String {
        val msg = "The factorial of %d is %d."
        return msg.format(x, factorial(x))
    }

    fun formatResult(name: String, n: Int, f: (n: Int) -> Int): String {
        val msg = "The %s of %d is %d."
        return msg.format(name, n, f(n))
    }
}


fun main(args: Array<String>) {
    println(MyObject.formatAbs(-42))
    println(MyObject.formatFactorial(7))
    println(MyObject.formatResult("algo",7, { n: Int -> MyObject.factorial(n) }))
}
