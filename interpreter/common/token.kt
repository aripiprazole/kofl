package com.lorenzoog.kofl.interpreter

enum class TokenType {
  // single-character tokens
  LeftParen, RightParen, LeftBrace,
  RightBrace, Comma, Dot, Minus, Plus,
  Semicolon, Slash, Star, At, Colon,

  // one-or-double-character tokens
  Bang, BangEqual,
  Equal, EqualEqual,
  Greater, GreaterEqual,
  Less, LessEqual,

  // comparison tokens
  And, Or,

  // literals tokens
  Identifier, String, Double, Int,

  // keywords tokens
  If, Else, This, False, True, While,
  Func, Val, Var, Return, Struct,
  Typedef, External,

  // special tokens
  Eof
}

data class Token(
  val type: TokenType,
  val lexeme: String,
  val literal: Any? = null,
  val line: Int
) {
  override fun toString() = when {
    literal != null -> "$literal"
    type == TokenType.Identifier -> lexeme
    type == TokenType.Eof -> "EOF"
    else -> "$type"
  }
}

fun String.asToken(): Token = Token(TokenType.Identifier, this, null, -1)

val Token.location: String
  get() = if (type == TokenType.Eof) "EOF" else "line $line"
