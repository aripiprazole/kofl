package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.interpreter.module.SourceCode

fun createEval(interpreter: Interpreter) = fun(code: String): SourceCode? {
  if (code.isEmpty()) return null

  return interpreter.run {
    val stmts = parse(code)
    val descriptors = compile(stmts)

    evaluate(descriptors)
  }
}
