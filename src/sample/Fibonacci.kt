package sample

// Exercise 1: Write a function to compute the nth fibonacci number

// 0 and 1 are the first two numbers in the sequence,
// so we start the accumulators with those.
// At every iteration, we add the two numbers to get the next one.
fun fib(n: Int): Int {
    tailrec fun loop(n: Int, prev: Int,cur: Int): Int =
            if(n == 0) prev
            else loop(n -1, cur, prev + cur)
    return loop(n, 0, 1)
}
