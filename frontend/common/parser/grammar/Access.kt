package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Access : Grammar<Expr>() {
  override val rule: Parser<Expr> = lazied { Access }

  private val Get = label("get")(
    combine(Token.Primary, many(Token.Dot with Token.Identifier)) { receiver, calls ->
      calls.fold(receiver) { acc, (_, expr) ->
        Expr.Get(acc, expr.name, line)
      }
    }
  )

  private val Call = label("call")(
    combine(Token.Primary, Token.LeftParen, Token.RightParen) { name, _, _ ->
      Expr.Call(name, mapOf(), line)
    }
  )

  private val Access = label("access")(
    Call or Get or Token.Primary
  )
}