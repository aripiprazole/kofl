package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*
import com.lorenzoog.kofl.frontend.Token as FToken

typealias ArgT = Triple<Expr.Var?, FToken, Expr>
typealias ArgsT = Pair<ArgT, List<Pair<FToken, ArgT>>>
typealias ParenthesisT = Triple<FToken, ArgsT?, FToken>
typealias CurlingArgsT = Pair<ParenthesisT, List<ParenthesisT>>

internal object Access : Grammar<Expr>() {
  override val rule: Parser<Expr> = lazied { Access }

  private val Get = label("get")(
    combine(Token.Primary, many(Token.Dot with Token.Identifier)) { receiver, calls ->
      calls.fold(receiver) { acc, (_, expr) ->
        Expr.Get(acc, expr.name, line)
      }
    }
  )

  private val Callee = label("callee")(Get or Token.Primary)

  private val Arg: Parser<ArgT> = label("argument")(
    Token.Identifier.optional() with Token(Token.Equal) with Token.Expression
  )

  private val Args: Parser<ArgsT> = label("arguments")(
    Arg with many(Token(Token.Comma) with Arg)
  )

  private val Parenthesis: Parser<ParenthesisT> = label("parenthesis")(
    Token.LeftParen with Args.optional() with Token.RightParen
  )

  private val CurlingArgs: Parser<CurlingArgsT> = label("curling-args")(
    Parenthesis with many(Parenthesis)
  )

  private val Call = label("call")(
    combine(Callee, CurlingArgs) { name, args ->
      val (headArgs, tailArgs) = args
      val (_, firstArgs) = headArgs

      fun handleArgs(list: ArgsT?): Map<FToken?, Expr> {
        val (head, tail) = list ?: return mapOf()

        return (listOf((null as FToken?) to head) + tail)
          .groupBy(
            keySelector = { (_, argument) -> argument.first?.name },
            valueTransform = { (_, argument) -> argument.third }
          )
          .mapValues { it.value.last() }
      }

      tailArgs.fold(Expr.Call(name, handleArgs(firstArgs), line)) { acc, (_, argList, _) ->
        Expr.Call(acc, handleArgs(argList), line)
      }
    }
  )

  private val Access = label("access")(
    Call or Get or Token.Primary
  )
}