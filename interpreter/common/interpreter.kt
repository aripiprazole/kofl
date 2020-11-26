package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

class Return(val value: KoflObject) : RuntimeException(null, null)

class Interpreter(private val debug: Boolean = false) {
  private val globalEnvironment = MutableEnvironment(NativeEnvironment())
  private val locals = mutableMapOf<Expr, Int>()
  private val typeEnvironment = globalEnvironment(24) {
    defineType("Unit", KoflUnit)
    defineType("Boolean", KoflBoolean)
    defineType("String", KoflString)
    defineType("Int", KoflInt)
    defineType("Double", KoflDouble)
  }

  fun lex(code: String): List<Token> {
    val scanner = Scanner(code)
    return scanner.scan()
  }

  fun parse(code: String, repl: Boolean = true): List<Stmt> {
    return Parser(lex(code), repl).parse()
  }

  fun eval(code: String, repl: Boolean = true): List<KoflObject> {
    val resolver = Resolver(locals)
    val evaluator = CodeEvaluator(locals, typeEnvironment.peek())
    val typeEvaluator = TypeChecker(evaluator, typeEnvironment)
    val stmts = parse(code, repl)

    if (repl && debug) println("AST: $stmts")

    typeEvaluator.visitStmts(stmts)
    resolver.resolve(stmts)
    return evaluator.eval(stmts, globalEnvironment).also {
      if(repl && debug) println("DUMP: $it")
    }
  }
}
