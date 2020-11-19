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
} catch (error: RuntimeError) {
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

// TODO: typechecking
fun run(code: String): Any? {
  if (code.isEmpty()) return null

  val scanner = Scanner(code)
  val parser = Parser(scanner.scan())

  return parser.parse().let { stmts ->
    SimpleAstDumper.dump(stmts)
    Evaluator.eval(stmts)
  }
}
