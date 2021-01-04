package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.frontend.parser.lib.*

typealias ArgT = Pair<Pair<Expr.Var, Token>?, Expr>
typealias ArgsT = Pair<ArgT, List<Pair<Token, ArgT>>>
typealias ParenthesisT = Triple<Token, ArgsT?, Token>
typealias CurlingArgsT = Pair<ParenthesisT, List<ParenthesisT>>

internal object Access : Grammar<Expr>() {
  override val rule: Parser<Expr> = lazied { Access }

  private val Get = label("get")(
    combine(Primary, many(Dot + Identifier)) { receiver, calls ->
      calls.fold(receiver) { acc, (_, expr) ->
        Expr.Get(acc, expr.name, line)
      }
    }
  )

  private val NamedArg: Parser<ArgT> = label("named-arg")((Identifier + Colon).optional() + Func)
  private val UnnamedArg: Parser<ArgT> = label("unnamed-arg")(nullable<Pair<Expr.Var, Token>?>() + Func)

  private val Arg: Parser<ArgT> = label("arg")(NamedArg or UnnamedArg)
  private val Args: Parser<ArgsT> = label("args")(Arg withPair many(Comma + Arg))

  private val ArgsParenthesis: Parser<ParenthesisT> = label("args-parenthesis")(
    LeftParen + Args.optional() + RightParen
  )

  private val EmptyParenthesis: Parser<ParenthesisT> = label("empty-parenthesis")(
    LeftParen + nullable<ArgsT>() + RightParen
  )

  private val Parenthesis: Parser<ParenthesisT> = label("parenthesis")(ArgsParenthesis or EmptyParenthesis)
  private val CurlingArgs: Parser<CurlingArgsT> = label("curling-args")(Parenthesis + many(Parenthesis))

  private val Call = label("call")(
    combine(label("callee")(Get or Primary), CurlingArgs) { name, args ->
      val (headArgs, tailArgs) = args
      val (_, firstArgs) = headArgs

      fun handleArgs(list: ArgsT?): Map<Token?, Expr> {
        val (head, tail) = list ?: return mapOf()

        return (listOf((null as Token?) to head) + tail)
          .groupBy(
            keySelector = { (_, argument) ->
              argument.first?.first?.name // argumentName
            },
            valueTransform = { (_, argument) ->
              argument.second // argumentValue
            }
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