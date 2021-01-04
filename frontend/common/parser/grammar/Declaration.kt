@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.frontend.parser.lib.*

internal object Declaration : Grammar<Stmt>() {
  fun handleVar(keyword: Parser<Token>): Parser<Triple<Token, Token?, Expr>> {
    val untypedVar =
      combine(keyword, Identifier, nullable<Pair<Token, Token>>(), Equal, Func, Semicolon) { _, (name), _, _, expr, _ ->
        Triple(name, null, expr)
      }

    val typedVar =
      combine(keyword, Identifier, (Colon + Identifier), Equal, Func, Semicolon) { _, (name), (_, type), _, expr, _ ->
        Triple(name, type.name, expr)
      }

    return untypedVar or typedVar
  }

  val Type = label("type")(combine(Colon, Identifier) { _, (name) -> name })
  val FuncDecl = label("func-decl")(Func.Func map { Stmt.ExprStmt(it, line) })

  val ClassDecl = label("class-decl")(
    combine(Keywords.Type, Keywords.Class, Identifier, Func.Parameters, Semicolon) { _, _, (name), parameters, _ ->
      Stmt.Type.Record(name, parameters, line)
    }
  )

  val ValDecl = label("val-decl")(
    handleVar(Keywords.Val).map { (name, type, expr) -> Stmt.ValDecl(name, type, expr, line) }
  )

  val VarDecl = label("var-decl")(
    handleVar(Keywords.Var).map { (name, type, expr) -> Stmt.VarDecl(name, type, expr, line) }
  )

  val Program = many(this) + EOF

  override val rule: Parser<Stmt> = label("decl")(
    FuncDecl or ValDecl or VarDecl or ClassDecl
  )
}