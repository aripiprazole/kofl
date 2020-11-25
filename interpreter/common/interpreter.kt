package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

class Return(val value: KoflObject) : RuntimeException(null, null)

class Interpreter {
  private val globalEnvironment = MutableEnvironment(NativeEnvironment())
  private val locals = mutableMapOf<Expr, Int>()

  fun lex(code: String): List<Token> {
    val scanner = Scanner(code)
    return scanner.scan()
  }

  fun parse(code: String, repl: Boolean = true): List<Stmt> {
    return Parser(lex(code), repl).parse()
  }

  fun eval(code: String, repl: Boolean = true): List<KoflObject> {
    val resolver = Resolver(locals)
    val evaluator = CodeEvaluator(locals)
    val typeEvaluator = TypeChecker(globalEnvironment(512_000) {
      defineType("Unit", KoflUnit)
      defineType("Boolean", KoflBoolean)
      defineType("String", KoflString)
      defineType("Int", KoflInt)
      defineType("Double", KoflDouble)
      defineFunction(
        name = "println", KoflCallable.Type(
          parameters = mapOf("message" to KoflString),
          returnType = KoflUnit
        )
      )
    })
    val stmts = parse(code, repl)

    if (repl) println("AST: $stmts")

    typeEvaluator.visit(stmts)
    resolver.resolve(stmts)
    return evaluator.eval(stmts, globalEnvironment)
  }
}
