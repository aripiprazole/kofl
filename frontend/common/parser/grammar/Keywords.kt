package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.text

internal object Keywords {
  val Func = token(text(TokenType.Func, "func"))
  val External = token(text(TokenType.External, "external"))
  val Return = token(text(TokenType.Return, "return"))
}