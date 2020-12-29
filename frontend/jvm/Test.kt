import com.lorenzoog.kofl.frontend.parser.grammar.Math
import com.lorenzoog.kofl.frontend.parser.lib.unwrap

/**
 * TODO: remove me
 */
fun main() {
  println(Math.parse("""10.0 + (5.0 + 4.0)""").unwrap())
}