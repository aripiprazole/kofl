import com.lorenzoog.kofl.frontend.parser.grammar.Access
import com.lorenzoog.kofl.frontend.parser.grammar.Func
import com.lorenzoog.kofl.frontend.parser.lib.EmptyContext
import com.lorenzoog.kofl.frontend.parser.lib.unwrapOr

/**
 * TODO: remove me
 */
fun main() {
  val input = """func main() = call(4)""".trimIndent()

  println(Func.parse(input).unwrapOr { result ->
    println("Expected: ${result.expected}; Actual: ${result.actual}; Char: ${input.getOrNull(result.actual.index)}")

    return
  })
}