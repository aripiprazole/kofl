package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.Grammar
import com.lorenzoog.kofl.frontend.parser.lib.Parser

internal object Access : Grammar<Expr>() {
  override val rule: Parser<Expr>
    get() = TODO("Not yet implemented")
}