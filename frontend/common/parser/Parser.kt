package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.grammar.Math
import com.lorenzoog.kofl.frontend.parser.grammar.Token
import com.lorenzoog.kofl.frontend.parser.grammar.line
import com.lorenzoog.kofl.frontend.parser.lib.*

// TODO: support left recursion in the lib
internal class ParserImpl(private val code: String) : Parser {
  fun parseImpl(): ParseResult<Expr> {
    return (Math with Token.EOF)(Context(code)).map { it.first }
  }

  override fun parse(): List<Stmt> {
    return listOf(Stmt.ExprStmt(parseImpl().unwrap(), line))
  }
}