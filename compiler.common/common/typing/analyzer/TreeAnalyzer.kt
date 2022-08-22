package me.devgabi.kofl.compiler.common.typing.analyzer

import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.Token

interface TreeAnalyzer {
  fun analyze(expr: Expr): KfType
  fun validate(stmt: Stmt)

  fun findOverload(name: Expr): List<KfType.Callable>
  fun findCallable(name: Expr, arguments: Map<Token?, Expr>): KfType.Callable

  fun validate(stmts: List<Stmt>) = stmts.forEach { validate(it) }
}
