@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.TokenType
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.eof
import me.devgabi.kofl.frontend.parser.lib.identifier
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.lazied
import me.devgabi.kofl.frontend.parser.lib.lexeme
import me.devgabi.kofl.frontend.parser.lib.map
import me.devgabi.kofl.frontend.parser.lib.numeric
import me.devgabi.kofl.frontend.parser.lib.or
import me.devgabi.kofl.frontend.parser.lib.regex
import me.devgabi.kofl.frontend.parser.lib.string
import me.devgabi.kofl.frontend.parser.lib.text
import me.devgabi.kofl.frontend.unescape

val Spaces = label("spaces")(
  regex("\\s+".toRegex()) or regex("\\r+".toRegex()) or regex("\\t+".toRegex())
)

val token = lexeme(label("junk")(regex("\\s*\\r*\\t*".toRegex())))

val Plus = token(text(TokenType.Plus, "+"))
val Minus = token(text(TokenType.Minus, "-"))
val Star = token(text(TokenType.Star, "*"))
val Slash = token(text(TokenType.Slash, "/"))
val Dot = token(text(TokenType.Dot, "."))
val Colon = token(text(TokenType.Colon, ":"))
val Bang = token(text(TokenType.Bang, "!"))
val Comma = token(text(TokenType.Comma, ","))
val And = token(text(TokenType.And, "&"))
val Or = token(text(TokenType.Or, "|"))
val LeftParen = token(text(TokenType.LeftParen, "("))
val RightParen = token(text(TokenType.RightParen, ")"))
val LeftBrace = token(text(TokenType.LeftBrace, "{"))
val RightBrace = token(text(TokenType.RightBrace, "}"))
val Equal = token(text(TokenType.Comma, "="))
val Semicolon = token(text(TokenType.Semicolon, ";"))
val Enter = text(TokenType.Enter, "\n")

val Greater = token(text(TokenType.Greater, ">"))
val Less = token(text(TokenType.Less, "<"))

val AndAnd = token(text(TokenType.And, "&&"))
val OrOr = token(text(TokenType.Or, "||"))
val GreaterEqual = token(text(TokenType.GreaterEqual, ">="))
val LessEqual = token(text(TokenType.LessEqual, "<="))
val BangEqual = token(text(TokenType.LeftBrace, "!="))
val EqualEqual = token(text(TokenType.RightBrace, "=="))
val SlashSlash = token(text(TokenType.SlashSlash, "//"))
val CommentStart = token(text(TokenType.CommentStart, "/*"))
val CommentEnd = text(TokenType.CommentEnd, "*/")

val EOF = eof(TokenType.Eof)

val Decimal = label("decimal")(numeric())

val Numeric = label("numeric")(
  token(Decimal) map {
    val value = it.toIntOrNull() ?: it.toDoubleOrNull() ?: 0

    Expr.Literal(value, line)
  }
)

val Text = label("text")(
  token(string(TokenType.String)).map {
    Expr.Literal(
      it.literal?.toString()?.unescape() ?: error(
        "$it should not be converted to Expr.Literal if it is null"
      ),
      line
    )
  }
)

val Identifier = label("identifier")(token(identifier(TokenType.Identifier))) map {
  Expr.Var(it, line)
}

val Group = label("group")(
  combine(LeftParen, token(lazied { Func }), RightParen) { _, value, _ ->
    Expr.Grouping(value, line)
  }
)

val This = label("this")(
  lazied { Keywords.This } map { Expr.ThisExpr(it, line) }
)

val Boolean = label("boolean")(
  lazied { Keywords.True or Keywords.False } map {
    Expr.Literal(it.literal.toString().toBoolean(), line)
  }
)

val Primary = label("primary")(
  This or Boolean or Identifier or Text or Numeric or Group
)
