import com.lorenzoog.kofl.frontend.debug.printAvlTree
import com.lorenzoog.kofl.frontend.parser.ParserImpl
import com.lorenzoog.kofl.frontend.parser.grammar.Declaration
import com.lorenzoog.kofl.frontend.parser.lib.parse
import com.lorenzoog.kofl.frontend.parser.lib.unwrapOr

/**
 * TODO: remove me
 */
fun main() {
  val input =
    """
    // main func
    /** AAAAA */
    func main(args: StringArray): Unit {
      type class Person(name: String);
      
      val s = "";
      val s: String = 4;
    
      println(s = a = 4);
      
      if 40 
        then println("")
        else println();
    
      func subFunction() {
    
      }
    
      return subFunction();
    }
    """.trimIndent()


  println("Result:")
  println(ParserImpl(input, false).parse().printAvlTree())
}
