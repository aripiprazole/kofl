import com.lorenzoog.kofl.frontend.parser.grammar.Math
import com.lorenzoog.kofl.frontend.parser.lib.unwrap

/**
 * TODO: remove me
 */
fun main() {
  println(Math.parse("40 + (50 / 2)").unwrap())
}