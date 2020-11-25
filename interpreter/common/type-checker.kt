package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

const val MAX_STACK = 512_000

class TypeChecker(
  private val types: Stack<TypeEnvironment> = Stack(MAX_STACK)
) : Expr.Visitor<KoflType>, Stmt.Visitor<KoflType> {
  private var currentFunc: KoflCallable.Type? = null

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
    return when (expr.value) {
      is String -> KoflString
      is Double -> KoflDouble
      is Int -> KoflInt
      is Boolean -> KoflBoolean
      else -> throw InvalidTypeException(expr.value::class.toString())
    }
  }

  override fun visitUnaryExpr(expr: Expr.Unary): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitVarExpr(expr: Expr.Var): KoflType {
    return types.peek().findName(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): KoflType {
    val callee = when (val callee = expr.calle) {
      is Expr.Var -> types.peek().findFunction(callee.name.lexeme)
      else -> visit(expr.calle)
    }

    if (callee !is KoflCallable.Type) throw CompileException("expected $callee to be a function")

    return callee.returnType
  }

  override fun visitGetExpr(expr: Expr.Get): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitSetExpr(expr: Expr.Set): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): KoflType {
    return funcType(expr.returnType?.lexeme, expr.arguments, expr.body)
  }

  private fun funcType(returnTypeStr: String?, arguments: Map<Token, Token>, body: List<Stmt>): KoflCallable.Type {
    val returnType = findTypeOrNull(returnTypeStr) ?: KoflUnit

    val returnStmt = body.filterIsInstance<Stmt.ReturnStmt>().firstOrNull()
    if(returnType != KoflUnit && returnStmt == null)  throw MissingReturnException()

    val gotType = returnStmt?.let { visit(it) } ?: KoflUnit

    if (gotType != returnType)
      throw InvalidDeclaredTypeException(gotType.toString(), returnType.toString())

    beginScope()
    visit(body)
    endScope()

    return KoflCallable.Type(
      parameters = arguments.mapKeys { (name) -> name.lexeme }.mapValues { (_, value) ->
        findType(value.lexeme)
      },
      returnType
    )
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): KoflType {
    return funcType(expr.returnType?.lexeme, expr.arguments, expr.body)
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): KoflType {
    return funcType(expr.returnType?.lexeme, expr.arguments, expr.body)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitIfExpr(expr: Expr.IfExpr): KoflType {
    val condition = visit(expr.condition)
    if (condition != KoflBoolean) throw InvalidDeclaredTypeException(condition.toString(), KoflBoolean.toString())

    val thenBranch = expr.thenBranch.also {
      beginScope()
      visit(it)
      endScope()
    }
    val thenLast = thenBranch.lastOrNull()
    val elseBranch = expr.elseBranch?.also {
      beginScope()
      visit(it)
      endScope()
    }
    val elseLast = elseBranch?.lastOrNull()

    if (thenBranch.isNotEmpty() && thenLast != null && elseBranch != null && elseBranch.isNotEmpty()) {
      val thenType = visit((thenLast as? Stmt.ExprStmt)?.expr ?: return KoflUnit)
      val elseType = visit((elseLast as? Stmt.ExprStmt)?.expr ?: return KoflUnit)

      if (thenType != elseType) {
        throw InvalidDeclaredTypeException(elseType.toString(), thenType.toString())
      }

      return thenType
    }

    return KoflUnit
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): KoflType {
    visit(stmt.expr)
    return KoflUnit
  }

  override fun visitBlockStmt(stmt: Stmt.Block): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): KoflType {
    TODO("Not yet implemented")
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): KoflType {
    return visit(stmt.expr)
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
    else findType(typeName).also {
      if (actualType != it) throw InvalidDeclaredTypeException(actualType.toString(), it.toString())
    }
  }

  private fun findTypeOrNull(typeName: String?): KoflType? {
    return types.peek().findTypeOrNull(typeName.toString())
  }

  private fun findType(typeName: String?): KoflType {
    return findTypeOrNull(typeName) ?: throw TypeNotFoundException(typeName.toString())
  }

  private fun beginScope() {
    types.push(TypeEnvironment(types.peek()))
  }

  private fun endScope() {
    types.pop()
  }

  override fun visitStructTypedefStmt(stmt: Stmt.TypeDef.Struct): KoflType {
//    val type = KoflStruct(stmt)
//    types.peek()[stmt.name.lexeme] = type
//    TODO
    return KoflUnit
  }
}