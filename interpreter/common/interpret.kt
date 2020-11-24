package com.lorenzoog.kofl.interpreter

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
    val declEvaluator = DeclEvaluator(locals, evaluator)
    val typeEvaluator = TypeEvaluator(
      mutableListOf(
        mutableMapOf(
          "String" to KoflString
        )
      )
    )
    val stmts = parse(code, repl)

    if (repl) println("AST: $stmts")
    if (!repl) declEvaluator.eval(stmts, globalEnvironment)
    typeEvaluator.visit(stmts)
    resolver.resolve(stmts)
    return evaluator.eval(stmts, globalEnvironment)
  }
}