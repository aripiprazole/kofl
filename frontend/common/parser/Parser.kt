package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.ParseException
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.grammar.Declaration
import com.lorenzoog.kofl.frontend.parser.grammar.Statement
import com.lorenzoog.kofl.frontend.parser.lib.Context
import com.lorenzoog.kofl.frontend.parser.lib.map
import com.lorenzoog.kofl.frontend.parser.lib.unwrap
import com.lorenzoog.kofl.frontend.parser.lib.unwrapOr

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
        throw ParseException(actual.location, expected, actual.input)
      }
      .unwrap()
      .filter { it !is Stmt.CommentDecl }
  }
}