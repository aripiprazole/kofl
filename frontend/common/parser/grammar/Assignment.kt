@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.Parser
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.or
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
