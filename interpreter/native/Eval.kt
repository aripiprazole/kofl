package com.lorenzoog.kofl.interpreter

fun createEval(interpreter: Interpreter) = fun(code: String): Int {
  if (code.isEmpty()) return 1

  return interpreter.run {
    val tokens = lex(code)
    val stmts = parse(tokens)
    val descriptors = compile(stmts)

    evaluate(descriptors).main(arrayOf())
  }
}
