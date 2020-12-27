package com.lorenzoog.kofl.frontend.parser

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Stmt

// TODO: support left recursion in the lib
internal class ParserImpl(private val code: String) : Parser {
  override fun parse(): List<Stmt> {
    TODO("")
  }
}