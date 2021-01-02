package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.interpreter.module.SourceCode

fun createEval(interpreter: Interpreter) = fun(code: String): SourceCode? {
  if (code.isEmpty()) return null

  return interpreter.run {
    val tokens = lex(code)
    val stmts = parse(tokens)
    val descriptors = compile(stmts)

    evaluate(descriptors)
  }
}
