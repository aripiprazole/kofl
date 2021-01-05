@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.frontend.parser.grammar

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.parser.lib.Grammar
import com.lorenzoog.kofl.frontend.parser.lib.Parser
import com.lorenzoog.kofl.frontend.parser.lib.combine
import com.lorenzoog.kofl.frontend.parser.lib.label
import com.lorenzoog.kofl.frontend.parser.lib.or
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Assignment : Grammar<Expr>() {
  val Set = label("set")(
    combine(Access, Dot, Identifier, Equal, Func) { receiver, _, (name), _, value ->
      Expr.Set(receiver, name, value, line)
    }
  )

  val Assign = label("assign")(
    combine(Identifier, Equal, Func) { (name), _, value ->
      Expr.Assign(name, value, line)
    }
  )

  override val rule: Parser<Expr> = label("assignment")(
    Assign or Set
  )
}
