package com.lorenzoog.kofl.interpreter

import platform.posix.exit
import platform.posix.fopen

fun main(args: Array<String>) = try {
  when {
    args.size > 1 -> {
      printerr("Usage: kofl [script]")
      exit(64)
    }
    args.size == 1 -> file(args[0])
    else -> repl()
  }
} catch (error: ParseError) {
  error.report()

  exit(65)
} catch (error: SyntaxError) {
  error.report()

  exit(65)
} catch (error: KoflRuntimeError) {
  error.report()

  exit(70)
}

fun file(name: String) {
  val file = fopen(name, "r")

  if (file == null) {
    printerr("File do not exists")
    return exit(66)
  }

  TODO("Handle file not implemented")
}

@kotlin.native.concurrent.ThreadLocal
private val globalEnvironment = MutableEnvironment(NativeEnvironment())

@kotlin.native.concurrent.ThreadLocal
private val locals = mutableMapOf<Expr, Int>()

// TODO: typechecking
fun eval(code: String): Any? {
  if (code.isEmpty()) return null

  val scanner = Scanner(code)
  val parser = Parser(scanner.scan())
  val resolver = Resolver(locals)
  val evaluator = CodeEvaluator(globalEnvironment, locals)
  val declEvaluator = DeclEvaluator(globalEnvironment, locals, evaluator)

  return parser.parse().let { stmts ->
    dump(stmts)
    declEvaluator.eval(stmts)
    resolver.resolve(stmts)
    evaluator.eval(stmts)
  }
}
