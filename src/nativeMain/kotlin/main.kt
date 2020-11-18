import platform.posix.*

const val TAB_CHAR = '\t'
const val WINDOWS_ENTER_CHAR = '\r'
const val SPACE_CHAR = ' '
const val ENTER_CHAR = '\n'

const val DUMP_COLOR = "\u001b[32m"
const val RESULT_COLOR = "\u001b[34m"
const val PROMPT_COLOR = "\u001b[37m"

fun main(args: Array<String>) = try {
  when {
    args.size > 1 -> {
      printerr("Usage: kofl [script]")
      exit(64)
    }
    args.size == 1 -> file(args[0])
    else -> repl()
  }
} catch (error: LanguageError) {
  error.report()

  exit(65)
}

fun file(name: String) {
  val file = fopen(name, "r")

  if (file == null) {
    printerr("File do not exists")
    return exit(66)
  }

  TODO("Handle file not implemented")
}

// TODO: use raw-mode
tailrec fun repl() {
  print(PROMPT_COLOR)
  print("> ")
  print(readLine()?.let {
    try {
      RESULT_COLOR + run(it)
    } catch (error: LanguageError) {
      error.report()

      null
    }
  })
  println()

  repl()
}

fun run(code: String): Expr? {
  val scanner = Scanner(code)
  val parser = Parser(scanner.scan())

  return parser.parse()?.also { expr ->
    AstDumper.dump(expr)
  }
}

