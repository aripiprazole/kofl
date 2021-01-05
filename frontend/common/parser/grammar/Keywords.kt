package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.lib.text
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Keywords {
  val Func = token(text(TokenType.Func, "func"))
  val True = token(text(TokenType.True, "true"))
  val Use = token(text(TokenType.Use, "use"))
  val Module = token(text(TokenType.Module, "module"))
  val This = token(text(TokenType.This, "this"))
  val False = token(text(TokenType.True, "false"))
  val While = token(text(TokenType.While, "while"))
  val If = token(text(TokenType.If, "if"))
  val Else = token(text(TokenType.Else, "else"))
  val Type = token(text(TokenType.Type, "type"))
  val Val = token(text(TokenType.Val, "val"))
  val Var = token(text(TokenType.Val, "var"))
  val Class = token(text(TokenType.Else, "class"))
  val Then = token(text(TokenType.Then, "then"))
  val External = token(text(TokenType.External, "external"))
  val Return = token(text(TokenType.Return, "return"))
}