package com.lorenzoog.kofl.interpreter

import kotlin.system.exitProcess

fun main() = try {
  repl()
} catch (error: ParseError) {
  error.report()

  exitProcess(65)
} catch (error: SyntaxError) {
  error.report()

  exitProcess(65)
} catch (error: KoflRuntimeError) {
  error.report()

  exitProcess(70)
}

fun repl() {
  println("KOFL's repl. Type :quit to exit the program. Enjoy it 😃")
  println()

  while (true) {
    print("kofl>")

    when (val line = readLine().orEmpty()) {
      ":quit" -> exitProcess(0)
      else -> try {
        eval(line) // interpret and run the provided code in the line
      } catch (error: KoflError) {
        error.report() // just report error to dont crash program
      }
    }

    println()
  }
}

@ThreadLocal
private val interpreter = Interpreter()

// TODO: typechecking
fun eval(code: String): Any? {
  if (code.isEmpty()) return null

  return interpreter.eval(code)
}
