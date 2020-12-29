package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.grammar.Math
import com.lorenzoog.kofl.frontend.parser.grammar.Token
import com.lorenzoog.kofl.frontend.parser.grammar.line
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult
import com.lorenzoog.kofl.frontend.parser.lib.map
import com.lorenzoog.kofl.frontend.parser.lib.unwrap
import com.lorenzoog.kofl.frontend.parser.lib.with

// TODO: support left recursion in the lib
internal class ParserImpl(private val code: String) : Parser {
  fun parseImpl(): ParseResult<Expr> {
    return Math.parse(code)
  }

  override fun parse(): List<Stmt> {
    return listOf(Stmt.ExprStmt(parseImpl().unwrap(), line))
  }
}