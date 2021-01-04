import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.grammar.Declaration
import com.lorenzoog.kofl.frontend.parser.lib.many
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
    
      println("");
      
      if 40 
        then println("")
        else println();
    
      func subFunction() {
    
      }
    
      return subFunction();
    }
    """.trimIndent()


  println("RESULT: " + Declaration.Program.parse(input).unwrapOr { result ->
    println("Expected: ${result.expected};")
    println("Actual: ${result.actual};")
    println("Char: ${input.getOrNull(result.actual.index)}")

    return
  })
}
