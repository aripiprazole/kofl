import com.lorenzoog.kofl.frontend.parser.grammar.Statement
import com.lorenzoog.kofl.frontend.parser.lib.unwrapOr

/**
 * TODO: remove me
 */
fun main() {
  val input =
    """
    func main(): Unit {
      type class Person(name: String);
      
      val s = "";
      val s: String = 4;
    
      println("");
      
      if 40 then println("") else 
        println()
    
      func subFunction() {
    
      }
    
      return subFunction();
    }
    """.trimIndent()

  println(Statement.parse(input).unwrapOr { result ->
    println("Expected: ${result.expected};")
    println("Actual: ${result.actual};")
    println("Char: ${input.getOrNull(result.actual.index)}")

    return
  })
}
