@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Token
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.Parser
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.many
import me.devgabi.kofl.frontend.parser.lib.nullable
import me.devgabi.kofl.frontend.parser.lib.optional
import me.devgabi.kofl.frontend.parser.lib.or
import me.devgabi.kofl.frontend.parser.lib.plus
import me.devgabi.kofl.frontend.parser.lib.withPair
import kotlin.native.concurrent.ThreadLocal

typealias ArgT = Pair<Pair<Expr.Var, Token>?, Expr>
typealias ArgsT = Pair<ArgT, List<Pair<Token, ArgT>>>
typealias ParenthesisT = Triple<Token, ArgsT?, Token>
typealias CurlingArgsT = Pair<ParenthesisT, List<ParenthesisT>>

@ThreadLocal
internal object Access : Grammar<Expr>() {
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

  val Get = label("get")(
    combine(Primary, many(Dot + Identifier)) { receiver, calls ->
      calls.fold(receiver) { acc, (_, expr) ->
        Expr.Get(acc, expr.name, line)
      }
    }
  )

  val NamedArg: Parser<ArgT> = label("named-arg")((Identifier + Colon).optional() + Func)
  val UnnamedArg: Parser<ArgT> = label("unnamed-arg")(nullable<Pair<Expr.Var, Token>?>() + Func)

  val Arg: Parser<ArgT> = label("arg")(NamedArg or UnnamedArg)
  val Args: Parser<ArgsT> = label("args")(Arg withPair many(Comma + Arg))

  val ArgsParenthesis: Parser<ParenthesisT> = label("args-parenthesis")(
    LeftParen + Args.optional() + RightParen
  )

  val EmptyParenthesis: Parser<ParenthesisT> = label("empty-parenthesis")(
    LeftParen + nullable<ArgsT>() + RightParen
  )

  val Parenthesis: Parser<ParenthesisT> = label("parenthesis")(ArgsParenthesis or EmptyParenthesis)
  val CurlingArgs: Parser<CurlingArgsT> = label("curling-args")(Parenthesis + many(Parenthesis))

  val Call = label("call")(
    combine(label("callee")(Get or Primary), CurlingArgs) { name, args ->
      val (headArgs, tailArgs) = args
      val (_, firstArgs) = headArgs

      tailArgs.fold(Expr.Call(name, handleArgs(firstArgs), line)) { acc, (_, argList, _) ->
        Expr.Call(acc, handleArgs(argList), line)
      }
    }
  )

  override val rule: Parser<Expr> = label("access")(
    Call or Get or Primary
  )
}
