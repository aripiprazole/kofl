package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.grammar.Declaration
import com.lorenzoog.kofl.frontend.parser.lib.Context
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult
import com.lorenzoog.kofl.frontend.parser.lib.map
import com.lorenzoog.kofl.frontend.parser.lib.unwrap

// TODO: support left recursion in the lib
internal class ParserImpl(private val code: String) : Parser {
  private fun parseImpl(): ParseResult<List<Stmt>> {
    return Declaration.Program(Context(code)).map { it.first }
  }

  override fun parse(): List<Stmt> {
    return parseImpl().unwrap()
  }
}