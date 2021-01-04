import com.lorenzoog.kofl.frontend.parser.grammar.Statement
import com.lorenzoog.kofl.frontend.parser.lib.unwrapOr

/**
 * TODO: remove me
 */
fun main() {
  val input = """if true then println("TRUE 😀") else println("FALSE 😥");""".trimIndent()

  println(Statement.parse(input).unwrapOr { result ->
    println("Expected: ${result.expected};")
    println("Actual: ${result.actual};")
    println("Char: ${input.getOrNull(result.actual.index)}")

    return
  })
}
