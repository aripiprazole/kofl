import platform.posix.exit
import platform.posix.fopen

fun main(args: Array<String>) = try {
  when {
    args.size > 1 -> {
      printerr("Usage: kofl [script]")
      exit(64)
    }
    args.size == 1 -> file(args[0])
    else -> {
      printHeader()
      repl()
    }
  }
} catch (error: LanguageError) {
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

// TODO: use raw-mode
tailrec fun repl() {
  print(WHILE_COLOR)
  print("> ")
  print(readLine()?.let {
    try {
      GREEN_COLOR + show(run(it))
    } catch (error: LanguageError) {
      error.report()
      null
    } catch (error: RuntimeError) {
      error.report()
      null
    }
  })
  println()

  repl()
}

// TODO: typechecking
fun run(code: String): Any? {
  if (code.isEmpty()) return null

  val scanner = Scanner(code)
  val parser = Parser(scanner.scan())

  return (parser.parse() ?: return null).let { expr ->
//    AstDumper.dump(expr)
    Evaluator.eval(expr)
  }
}

private fun printHeader() {
  print(MAGENTA_COLOR)
  println(
    """
=============================================================================
 _  ______  ______ _      
| |/ / __ \|  ____| |     
| ' / |  | | |__  | |     
|  <| |  | |  __| | |     
| . \ |__| | |    | |____ 
|_|\_\____/|_|    |______| REPL

Type exit to quit. // TODO

=============================================================================
  """
  )
  print(WHILE_COLOR)
}
