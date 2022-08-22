@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.Token
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.lazied
import me.devgabi.kofl.frontend.parser.lib.many
import me.devgabi.kofl.frontend.parser.lib.nullable
import me.devgabi.kofl.frontend.parser.lib.optional
import me.devgabi.kofl.frontend.parser.lib.or
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Func : Grammar<Expr>() {
  fun handleParameters(parameterList: List<Pair<Expr.Var, Expr.Var>>): Map<Token, Token> {
    return parameterList
      .groupBy(
        keySelector = { it.first.name },
        valueTransform = { it.second.name }
      )
      .mapValues { it.value.first() }
  }

  val ReturnType = label("return-type")(combine(Colon, Identifier) { _, (name) -> name })
  val Parameter = label("parameter")(
    combine(Identifier, Colon, Identifier) { name, _, type -> name to type }
  )

  val Parameters = label("parameters")(
    combine(LeftParen, many(Parameter), RightParen) { _, parameters, _ ->
      handleParameters(
        parameters
      )
    }
  )

  val ExpressionBody = label("expression-body")(
    combine(Equal, token(this)) { _, expr -> listOf(Stmt.ReturnStmt(expr, line)) }
  )

  val Body = label("body")(
    ExpressionBody or combine(
      LeftBrace,
      many(lazied { Statement }),
      RightBrace
    ) { _, body, _ -> body }
  )

  val NativeFunc = label("native-fun")(
    combine(
      Keywords.External,
      Keywords.Func,
      Identifier,
      Parameters,
      ReturnType.optional()
    ) { _, _, (name), parameters, returnType ->
      Expr.NativeFunc(name, parameters, returnType, line)
    }
  )

  val ExtensionFunc = label("extension-func")(
    run {
      val untypedExtensionFunc = label("untyped-extension-func")(
        combine(
          Keywords.Func,
          Identifier,
          Spaces,
          Identifier,
          Parameters,
          nullable<Token>(),
          Body
        ) { _, (receiverName), _, (funcName), parameters, _, body ->
          Expr.ExtensionFunc(receiverName, funcName, parameters, body, null, line)
        }
      )

      untypedExtensionFunc or
        combine(
          Keywords.Func,
          Identifier,
          Spaces,
          Identifier,
          Parameters,
          ReturnType,
          Body
        ) { _, (receiverName), _, (funcName), parameters, returnType, body ->
          Expr.ExtensionFunc(receiverName, funcName, parameters, body, returnType, line)
        }
    }
  )

  val AnonymousFunc = label("anonymous-func")(
    run {
      val untypedAnonymousFunc = label("untyped-anonymous-func")(
        combine(Keywords.Func, Parameters, nullable<Token>(), Body) { _, parameters, _, body ->
          Expr.AnonymousFunc(parameters, body, null, line)
        }
      )

      untypedAnonymousFunc or
        combine(Keywords.Func, Parameters, ReturnType, Body) { _, parameters, returnType, body ->
          Expr.AnonymousFunc(parameters, body, returnType, line)
        }
    }
  )

  val TypedCommonFunc = label("common-func")(
    run {
      val untypedCommonFunc = label("untyped-common-func")(
        combine(
          Keywords.Func,
          Identifier,
          Parameters,
          nullable<Token>(),
          Body
        ) { _, (name), parameters, _, body ->
          Expr.CommonFunc(name, parameters, body, null, line)
        }
      )

      untypedCommonFunc or
        combine(
          Keywords.Func,
          Identifier,
          Parameters,
          ReturnType,
          Body
        ) { _, (name), parameters, returnType, body ->
          Expr.CommonFunc(name, parameters, body, returnType, line)
        }
    }
  )

  val NamedFunc = label("named-func")(
    ExtensionFunc or NativeFunc or TypedCommonFunc
  )

  override val rule = (
    NamedFunc
      or AnonymousFunc
      or lazied { If }
      or lazied { Assignment }
      or Logical
    )
}
