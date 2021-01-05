import com.lorenzoog.kofl.frontend.debug.printAvlTree
import com.lorenzoog.kofl.frontend.parser.ParserImpl

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
