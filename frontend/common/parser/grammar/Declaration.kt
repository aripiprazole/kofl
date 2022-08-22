@file:Suppress("MemberVisibilityCanBePrivate")

package me.devgabi.kofl.frontend.parser.grammar

import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.frontend.Token
import me.devgabi.kofl.frontend.parser.lib.Grammar
import me.devgabi.kofl.frontend.parser.lib.Parser
import me.devgabi.kofl.frontend.parser.lib.anything
import me.devgabi.kofl.frontend.parser.lib.combine
import me.devgabi.kofl.frontend.parser.lib.label
import me.devgabi.kofl.frontend.parser.lib.many
import me.devgabi.kofl.frontend.parser.lib.map
import me.devgabi.kofl.frontend.parser.lib.nullable
import me.devgabi.kofl.frontend.parser.lib.or
import me.devgabi.kofl.frontend.parser.lib.plus
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal object Declaration : Grammar<Stmt>() {
  fun handleVar(keyword: Parser<Token>): Parser<Triple<Token, Token?, Expr>> {
    val untypedVar =
      combine(
        keyword,
        Identifier,
        nullable<Pair<Token, Token>>(),
        Equal,
        Func
      ) { _, (name), _, _, expr ->
        Triple(name, null, expr)
      }

    val typedVar =
      combine(
        keyword,
        Identifier,
        (Colon + Identifier),
        Equal,
        Func
      ) { _, (name), (_, type), _, expr ->
        Triple(name, type.name, expr)
      }

    return semicolon(untypedVar or typedVar)
  }

  val Type = label("type")(combine(Colon, Identifier) { _, (name) -> name })

  val UseDecl = label("use-decl")(
    semicolon(
      combine(Keywords.Use, Identifier) { _, (moduleName) ->
        Stmt.UseDecl(moduleName, line)
      }
    )
  )

  val ModuleDecl = label("module-decl")(
    semicolon(
      combine(Keywords.Module, Identifier) { _, (moduleName) ->
        Stmt.ModuleDecl(moduleName, line)
      }
    )
  )

  val FuncDecl = label("func-decl")(
    optionalSemicolon(Func.NamedFunc map { Stmt.ExprStmt(it, line) })
  )

  val ClassDecl = label("class-decl")(
    semicolon(
      combine(
        Keywords.Type,
        Keywords.Class,
        Identifier,
        Func.Parameters
      ) { _, _, (name), parameters ->
        Stmt.Type.Record(name, parameters, line)
      }
    )
  )

  val ValDecl = label("val-decl")(
    handleVar(Keywords.Val).map { (name, type, expr) -> Stmt.ValDecl(name, type, expr, line) }
  )

  val VarDecl = label("var-decl")(
    handleVar(Keywords.Var).map { (name, type, expr) -> Stmt.VarDecl(name, type, expr, line) }
  )

  val MultilineCommentDecl = label("comment-decl")(
    combine(CommentStart, many(anything(stopIn = CommentEnd)), CommentEnd) { _, chars, _ ->
      Stmt.CommentDecl(chars.toCharArray().concatToString(), line)
    }
  )

  val CommentDecl = label("comment-decl")(
    MultilineCommentDecl or combine(
      SlashSlash,
      many(anything(stopIn = Enter)),
      Enter
    ) { _, chars, _ ->
      Stmt.CommentDecl(chars.toCharArray().concatToString(), line)
    }
  )

  val Program = many(this) + EOF

  override val rule: Parser<Stmt> = label("decl")(
    CommentDecl
      or UseDecl
      or ModuleDecl
      or FuncDecl
      or ValDecl
      or VarDecl
      or ClassDecl
  )
}
