package com.lorenzoog.kofl.interpreter

val interpreter = Interpreter(debug = true, repl = true)

fun main(args: Array<String>) {
  interpreter.execute(
    """
    | external func println(message: String);
    | 
    | println();
    """.trimIndent()
  ).main(args)
}