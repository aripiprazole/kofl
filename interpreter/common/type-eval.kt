package com.lorenzoog.kofl.interpreter

class TypeNotFoundException(name: String) : RuntimeException("Type $name not found!")
class InvalidDeclaredTypeException(current: String, expected: String) :
  RuntimeException("Excepted $expected but got $current")

class InvalidTypeException(value: Any) : RuntimeException("invalid kofl type in $value")

class TypeEvaluator(
  val types: MutableList<MutableMap<String, KoflType>> = ArrayList(),
  val decls: MutableList<MutableMap<Expr, KoflType>> = ArrayList()
) : Expr.Visitor<KoflType>, Stmt.Visitor<KoflType> {
  var currentDepth = 0

  override fun visitAssignExpr(expr: Expr.Assign): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitBinaryExpr(expr: Expr.Binary): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitLogicalExpr(expr: Expr.Logical): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitLiteralExpr(expr: Expr.Literal): KoflType {
    println("LITERAL: ${expr.value::class.simpleName}('${expr.value}')")
    return when (expr.value) {
      is String -> KoflString
      is Double -> KoflDouble
      is Int -> KoflInt
      else -> throw InvalidTypeException(expr.value)
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitVarExpr(expr: Expr.Var): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitCallExpr(expr: Expr.Call): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitGetExpr(expr: Expr.Get): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitSetExpr(expr: Expr.Set): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.Func): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitIfExpr(expr: Expr.IfExpr): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitBlockStmt(stmt: Stmt.Block): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): KoflType {
    return varType(stmt.type?.lexeme, stmt.value)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): KoflType {
    return varType(stmt.type?.lexeme, stmt.value)
  }

  private fun varType(typeName: String?, value: Expr): KoflType {
    val actualType = visit(value)

    return if (typeName == null) actualType
    else (types[currentDepth][typeName] ?: throw TypeNotFoundException(typeName)).also {
      if (actualType != it)
        throw InvalidDeclaredTypeException(typeName, it.toString())
    }
  }

  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct): KoflType {
    val type = KoflStruct(stmt)
    types[currentDepth][stmt.name.lexeme] = type
    return type
  }
}