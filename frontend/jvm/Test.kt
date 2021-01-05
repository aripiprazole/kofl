import com.lorenzoog.kofl.frontend.debug.printAvlTree
import com.lorenzoog.kofl.frontend.parser.ParserImpl
import com.lorenzoog.kofl.frontend.parser.grammar.Text
import com.lorenzoog.kofl.frontend.parser.lib.parse

/**
 * TODO: remove me
 */
fun main() {
  val input =
    """
    func main(): Int {
        println("Hello, world!");

        return 0;
    }
    """.trimIndent()

  println("Result:")
  println(ParserImpl(input, false).parse().printAvlTree())
}
