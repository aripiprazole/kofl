package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.*
import com.lorenzoog.kofl.frontend.Token

typealias ArgT = Triple<Expr.Var?, Token, Expr>
typealias ArgsT = Pair<ArgT, List<Pair<Token, ArgT>>>
typealias ParenthesisT = Triple<Token, ArgsT?, Token>
typealias CurlingArgsT = Pair<ParenthesisT, List<ParenthesisT>>

internal object Expression : Grammar<Expr>() {
  override val rule: Parser<Expr> = lazied { Access }

  private val Get = label("get")(
    combine(Primary, many(Dot with Identifier)) { receiver, calls ->
      calls.fold(receiver) { acc, (_, expr) ->
        Expr.Get(acc, expr.name, line)
      }
    }
  )

  private val Callee = label("callee")(Get or Primary)

  private val Arg: Parser<ArgT> = label("argument")(
    Identifier.optional() with token(Equal) with Expression
  )

  private val Args: Parser<ArgsT> = label("arguments")(
    Arg with many(token(Comma) with Arg)
  )

  private val Parenthesis: Parser<ParenthesisT> = label("parenthesis")(
    LeftParen with Args.optional() with RightParen
  )

  private val CurlingArgs: Parser<CurlingArgsT> = label("curling-args")(
    Parenthesis with many(Parenthesis)
  )

  private val Call = label("call")(
    combine(Callee, CurlingArgs) { name, args ->
      val (headArgs, tailArgs) = args
      val (_, firstArgs) = headArgs

      fun handleArgs(list: ArgsT?): Map<Token?, Expr> {
        val (head, tail) = list ?: return mapOf()

        return (listOf((null as Token?) to head) + tail)
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
    Call or Get or Primary
  )
}