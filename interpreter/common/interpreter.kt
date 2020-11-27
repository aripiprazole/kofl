package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

class Interpreter(private val debug: Boolean = false) {
  private val globalEnvironment = MutableEnvironment(NativeEnvironment())
  private val locals = mutableMapOf<Expr, Int>()
  private val types = Stack<TypeEnvironment>(24).also { stack ->
    stack.push(TypeEnvironment().apply {
      defineType("Unit", KoflUnit)
      defineType("Boolean", KoflBoolean)
      defineType("String", KoflString)
      defineType("Int", KoflInt)
      defineType("Double", KoflDouble)
      defineType("Any", KoflAny)
    })
  }

  public fun lex(code: String): List<Token> {
    val scanner = Scanner(code)
    return scanner.scan().also { scanned ->
      if (debug)
        println("TOKENS: $scanned")
    }
  }

  public fun parse(code: String, repl: Boolean = true): List<Stmt> {
    return Parser(lex(code), repl).parse()
  }

  public fun eval(code: String, repl: Boolean = true): List<KoflObject> {
    val resolver = Resolver(locals)
    val evaluator = CodeEvaluator(locals, types.peek())
    val typeEvaluator = TypeChecker(evaluator, types)
    val stmts = parse(code, repl)

    if (repl && debug)
      println("AST: $stmts")

    typeEvaluator.visitStmts(stmts)
    resolver.resolve(stmts)

    return evaluator.eval(stmts, globalEnvironment).also { objects ->
      if (repl && debug)
        println("DUMP: $objects")
    }
  }
}
