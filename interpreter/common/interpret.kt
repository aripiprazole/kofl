package com.lorenzoog.kofl.interpreter

object Interpreter {
  fun lex(code: String): List<Token> {
    val scanner = Scanner(code)
    return scanner.scan()
  }

  fun parse(code: String, repl: Boolean = true): List<Stmt> {
    return Parser(lex(code), repl).parse()
  }
}