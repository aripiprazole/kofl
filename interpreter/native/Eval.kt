package com.lorenzoog.kofl.interpreter

fun createEval(interpreter: Interpreter) = fun(code: String): Int {
  if (code.isEmpty()) return 1

  return interpreter.run {
    val stmts = parse(code)
    val descriptors = compile(stmts)

    evaluate(descriptors).main(arrayOf())
  }
}
