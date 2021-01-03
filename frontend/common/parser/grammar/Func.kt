package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Func : Grammar<Expr>() {
  private val ReturnType = label("return-type")(
    combine(Colon, Identifier) { _, (name) ->
      name
    }
  )

  private val Parameter = label("parameter")(
    combine(Identifier, Colon, Identifier) { name, _, type ->
      name to type
    }
  )

  private val Parameters = label("parameters")(
    combine(LeftParen, many(Parameter), RightParen) { _, parameters, _ ->
      parameters
    }
  )

  private val ExpressionBody = label("expression-body")(
    combine(Equal, token(this)) { _, expr ->
      listOf(Stmt.ExprStmt(expr, line))
    }
  )

  private val Body = label("body")(
    ExpressionBody or combine(LeftBrace, many(Statement), RightBrace) { _, body, _ ->
      body
    }
  )

  private val NativeFunc = label("common-fun")(
    combine(
      Keywords.External, Keywords.Func, Identifier, Parameters, ReturnType.optional()
    ) { _, _, (name), parameterList, returnType ->
      val parameters = parameterList
        .groupBy(
          keySelector = { it.first.name },
          valueTransform = { it.second.name }
        )
        .mapValues { it.value.first() }

      Expr.NativeFunc(name, parameters, returnType, line)
    }
  )

  private val CommonFunc = label("common-func")(
    combine(
      Keywords.Func, Identifier, Parameters, ReturnType.optional(), Body
    ) { _, (name), parameterList, returnType, body ->
      val parameters = parameterList
        .groupBy(
          keySelector = { it.first.name },
          valueTransform = { it.second.name }
        )
        .mapValues { it.value.first() }

      Expr.CommonFunc(name, parameters, body, returnType, line)
    }
  )

  override val rule: Parser<Expr> = NativeFunc or CommonFunc or Logical
}