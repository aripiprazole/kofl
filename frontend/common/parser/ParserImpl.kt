package me.devgabi.kofl.frontend.parser

import me.devgabi.kofl.frontend.KoflParseException
import me.devgabi.kofl.frontend.Parser
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.parser.grammar.Declaration
import me.devgabi.kofl.frontend.parser.grammar.Statement
import me.devgabi.kofl.frontend.parser.lib.Context
import me.devgabi.kofl.frontend.parser.lib.map
import me.devgabi.kofl.frontend.parser.lib.unwrap
import me.devgabi.kofl.frontend.parser.lib.unwrapOr

// TODO: support left recursion in the lib
internal class ParserImpl(code: String, private val repl: Boolean) : Parser {
  private val ctx = Context(input = code, index = 0, original = code)

  override fun parse(): List<Stmt> {
    val parse = if (repl) {
      Statement.REPL
    } else {
      Declaration.Program
    }

    return parse(ctx)
      .map { it.first }
      .unwrapOr { (expected, actual) ->
        throw KoflParseException(actual.location, expected, actual.input)
      }
      .unwrap()
      .filter { it !is Stmt.CommentDecl }
  }
}
