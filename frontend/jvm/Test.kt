import com.lorenzoog.kofl.frontend.parser.ParserImpl

/**
 * TODO: remove me
 */
fun main() {
  val parser = ParserImpl("""(record.call)()""".trimIndent())

  println(parser.parseImpl())
}