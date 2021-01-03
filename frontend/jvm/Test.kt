import com.lorenzoog.kofl.frontend.parser.ParserImpl

/**
 * TODO: remove me
 */
fun main() {
  val parser = ParserImpl("""record.call ( name = 1, another = "" ) ( anotherOne = 4 )""".trimIndent())

  println(parser.parseImpl())
}