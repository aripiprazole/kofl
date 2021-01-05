package com.lorenzoog.kofl.compiler.common.typing.analyzer

import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token

interface TreeAnalyzer {
  fun analyze(expr: Expr): KfType
  fun validate(stmt: Stmt)

  fun findOverload(name: Expr): List<KfType.Callable>
  fun findCallable(name: Expr, arguments: Map<Token?, Expr>): KfType.Callable

  fun validate(stmts: List<Stmt>) = stmts.forEach { validate(it) }
}
