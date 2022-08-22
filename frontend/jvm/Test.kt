import me.devgabi.kofl.frontend.debug.printAvlTree
import me.devgabi.kofl.frontend.parser.ParserImpl

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
