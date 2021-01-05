package com.lorenzoog.kofl.frontend.debug

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stmt

val Any.fields
  get(): Map<String, Any?> = when (this) {
    is Stmt -> fields
    is Expr -> fields
    is List<*> -> withIndex()
      .groupBy(keySelector = { it.index.toString() }, valueTransform = { it.value })
      .mapValues { it.value.first() }
    else -> mapOf()
  }

val Expr.fields
  get(): Map<String, Any?> = when (this) {
    is Expr.Assign -> mapOf(
      "name" to name,
      "value" to value,
      "line" to line
    )
    is Expr.Binary -> mapOf(
      "op" to op,
      "left" to left,
      "right" to right,
      "line" to line
    )
    is Expr.Logical -> mapOf(
      "op" to op,
      "left" to left,
      "right" to right,
      "line" to line
    )
    is Expr.Grouping -> mapOf(
      "expr" to expr,
      "line" to line
    )
    is Expr.Literal -> mapOf(
      "value" to value,
      "type" to value::class.simpleName,
      "line" to line
    )
    is Expr.Unary -> mapOf(
      "op" to op,
      "right" to right,
      "line" to line
    )
    is Expr.Var -> mapOf(
      "name" to name,
      "line" to line
    )
    is Expr.Call -> mapOf(
      "callee" to calle,
      "arguments" to arguments,
      "line" to line
    )
    is Expr.Get -> mapOf(
      "receiver" to receiver,
      "name" to name,
      "line" to line,
    )
    is Expr.Set -> mapOf(
      "receiver" to receiver,
      "name" to name,
      "value" to value,
      "line" to line,
    )
    is Expr.ThisExpr -> mapOf(
      "keyword" to keyword,
      "line" to line
    )
    is Expr.IfExpr -> mapOf(
      "condition" to condition,
      "thenBranch" to thenBranch,
      "elseBranch" to elseBranch,
      "line" to line
    )
    is Expr.CommonFunc -> mapOf(
      "name" to name,
      "parameters" to parameters,
      "returnType" to returnType,
      "body" to body,
      "line" to line
    )
    is Expr.ExtensionFunc -> mapOf(
      "receiver" to receiver,
      "name" to name,
      "parameters" to parameters,
      "returnType" to returnType,
      "body" to body,
      "line" to line
    )
    is Expr.AnonymousFunc -> mapOf(
      "parameters" to parameters,
      "returnType" to returnType,
      "body" to body,
      "line" to line
    )
    is Expr.NativeFunc -> mapOf(
      "name" to name,
      "parameters" to parameters,
      "returnType" to returnType,
      "line" to line
    )
  }

val Stmt.fields
  get(): Map<String, Any?> = when (this) {
    is Stmt.ModuleDecl -> mapOf("module" to module, "line" to line)
    is Stmt.UseDecl -> mapOf("module" to module, "line" to line)
    is Stmt.ExprStmt -> mapOf("expr" to expr, "line" to line)
    is Stmt.Block -> mapOf("body" to body, "line" to line)
    is Stmt.WhileStmt -> mapOf("condition" to condition, "body" to body, "line" to line)
    is Stmt.ReturnStmt -> mapOf("expr" to expr, "line" to line)
    is Stmt.CommentDecl -> mapOf("content" to content, "line" to line)
    is Stmt.ValDecl -> mapOf("name" to name, "type" to type, "value" to value, "line" to line)
    is Stmt.VarDecl -> mapOf("name" to name, "type" to type, "value" to value, "line" to line)
    is Stmt.Type.Record -> mapOf("name" to name, "parameters" to parameters, "line" to line)
  }
