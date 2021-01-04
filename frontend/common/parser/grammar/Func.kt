@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.frontend.parser.lib.*

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
  val Parameter = label("parameter")(combine(Identifier, Colon, Identifier) { name, _, type -> name to type })

  val Parameters = label("parameters")(
    combine(LeftParen, many(Parameter), RightParen) { _, parameters, _ -> handleParameters(parameters) }
  )

  val ExpressionBody = label("expression-body")(
    combine(Equal, token(this)) { _, expr -> listOf(Stmt.ReturnStmt(expr, line)) }
  )

  val Body = label("body")(
    ExpressionBody or combine(LeftBrace, many(lazied { Statement }), RightBrace) { _, body, _ -> body }
  )

  val NativeFunc = label("native-fun")(
    combine(
      Keywords.External, Keywords.Func, Identifier, Parameters, ReturnType.optional()
    ) { _, _, (name), parameters, returnType ->
      Expr.NativeFunc(name, parameters, returnType, line)
    }
  )

  val UntypedCommonFunc = label("untyped-common-func")(
    combine(Keywords.Func, Identifier, Parameters, nullable<Token>(), Body) { _, (name), parameters, _, body ->
      Expr.CommonFunc(name, parameters, body, null, line)
    }
  )

  val TypedCommonFunc = label("common-func")(
    UntypedCommonFunc or
      combine(Keywords.Func, Identifier, Parameters, ReturnType, Body) { _, (name), parameters, returnType, body ->
        Expr.CommonFunc(name, parameters, body, returnType, line)
      }
  )

  val Func = NativeFunc or TypedCommonFunc

  override val rule = NativeFunc or TypedCommonFunc or lazied { If } or Logical
}