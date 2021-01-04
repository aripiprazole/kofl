package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.grammar.Declaration
import com.lorenzoog.kofl.frontend.parser.grammar.Statement
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult
import com.lorenzoog.kofl.frontend.parser.lib.map
import com.lorenzoog.kofl.frontend.parser.lib.parse
import com.lorenzoog.kofl.frontend.parser.lib.unwrap

// TODO: support left recursion in the lib
internal class ParserImpl(private val code: String, private val repl: Boolean) : Parser {
  private fun parseImpl(): ParseResult<List<Stmt>> {
    if (repl) {
      return Statement.REPL.parse(code).map { it.first }
    }

    return Declaration.Program.parse(code).map { it.first }
  }

  override fun parse(): List<Stmt> {
    return parseImpl().unwrap()
  }
}