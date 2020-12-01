package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stack
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.interpreter.exceptions.KoflCompileException
import com.lorenzoog.kofl.interpreter.typing.KoflType
import com.lorenzoog.kofl.interpreter.typing.TypeContainer
import com.lorenzoog.kofl.interpreter.typing.TypeValidator
import com.lorenzoog.kofl.interpreter.typing.match

class Compiler(
  locals: MutableMap<Descriptor, Int>,
  private val container: Stack<TypeContainer>
) : Expr.Visitor<Descriptor>, Stmt.Visitor<Descriptor> {
  private val emitter = Emitter()
  private val validator = TypeValidator(locals, container)

  fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    visitStmts(stmts).map {
      emitter.emit(it)
    }

    return emitter.compiled()
  }

  override fun visitAssignExpr(expr: Expr.Assign): Descriptor {
    val type = validator.visitAssignExpr(expr)
    val name = expr.name.lexeme
    val value = visitExpr(expr.value)

    return AssignDescriptor(name, value, type)
  }

  override fun visitBinaryExpr(expr: Expr.Binary): Descriptor {
    val type = validator.visitBinaryExpr(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return BinaryDescriptor(left, op, right, type)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): Descriptor {
    val type = validator.visitLogicalExpr(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return LogicalDescriptor(left, op, right, type)
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): Descriptor {
    return visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): Descriptor {
    val type = validator.visitLiteralExpr(expr)

    return ConstDescriptor(expr.value, type)
  }

  override fun visitUnaryExpr(expr: Expr.Unary): Descriptor {
    val type = validator.visitUnaryExpr(expr)
    val op = expr.op.type
    val right = visitExpr(expr.right)

    return UnaryDescriptor(op, right, type)
  }

  override fun visitVarExpr(expr: Expr.Var): Descriptor {
    validator.visitVarExpr(expr)

    return GlobalVarDescriptor(expr.name.lexeme)
  }

  override fun visitCallExpr(expr: Expr.Call): Descriptor {
    val overload = validator.findCallOverload(expr.calle)
    val type = validator.findCallCallee(expr.calle, expr.arguments)
    val returnType = validator.visitCallExpr(expr)

    val index = overload.indexOf(type)

    val callee = when (val callee = expr.calle) {
      is Expr.Var -> GlobalVarDescriptor("${callee.name.lexeme}-$index")
//      TODO
//      is Expr.Get -> {
//        GetDescriptor(visitExpr(callee.receiver), callee.name.lexeme)
//      }
      else -> visitExpr(expr.calle)
    }

    val arguments = expr.arguments.entries.mapIndexed { i, (_, expr) ->
      val name = type.parameters.entries.toList().getOrNull(i)?.key
        ?: throw KoflCompileException.UnresolvedParameter(i)

      val descriptor = visitExpr(expr)

      name to descriptor
    }
    return CallDescriptor(callee, mapOf(*arguments.toTypedArray()), returnType)
  }

  override fun visitGetExpr(expr: Expr.Get): Descriptor {
    val type = validator.visitGetExpr(expr)
    val receiver = visitExpr(expr.receiver)
    val name = expr.name.lexeme

    return GetDescriptor(receiver, name, type)
  }

  override fun visitSetExpr(expr: Expr.Set): Descriptor {
    val type = validator.visitSetExpr(expr)
    val receiver = visitExpr(expr.receiver)
    val value = visitExpr(expr.value)
    val name = expr.name.lexeme

    return SetDescriptor(receiver, name, value, type)
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): Descriptor {
    return ThisDescriptor()
  }

  override fun visitIfExpr(expr: Expr.IfExpr): Descriptor {
    val type = validator.visitIfExpr(expr)
    val condition = visitExpr(expr.condition)
    val then = visitStmts(expr.thenBranch)
    val orElse = visitStmts(expr.elseBranch ?: emptyList())

    return IfDescriptor(condition, then, orElse, type)
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): Descriptor {
    val name = expr.name.lexeme
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return FunctionDescriptor(indexedName, parameters, returnType, scoped {
      visitStmts(expr.body)
    })
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): Descriptor {
    val name = expr.name.lexeme
    val parameters = mapOf("this" to findType(expr.receiver.lexeme)) + typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return FunctionDescriptor(name.indexed(index), parameters, returnType, scoped {
      visitStmts(expr.body)
    })
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): Descriptor {
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)

    return LocalFunctionDescriptor(parameters, returnType, scoped {
      visitStmts(expr.body)
    })
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): Descriptor {
    val name = expr.name.lexeme
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return NativeFunctionDescriptor(indexedName, parameters, returnType, name)
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): Descriptor {
    return visitExpr(stmt.expr)
  }

  override fun visitBlockStmt(stmt: Stmt.Block): Descriptor {
    return BlockDescriptor(scoped {
      visitStmts(stmt.body)
    })
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): Descriptor {
    val condition = visitExpr(stmt.condition)

    return WhileDescriptor(condition, scoped {
      visitStmts(stmt.body)
    })
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): Descriptor {
    val type = validator.visitReturnStmt(stmt)
    val value = visitExpr(stmt.expr)

    return ReturnDescriptor(value, type)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): Descriptor {
    val type = validator.visitValDeclStmt(stmt)
    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return ValDescriptor(name, value, type)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): Descriptor {
    val type = validator.visitVarDeclStmt(stmt)
    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return VarDescriptor(name, value, type)
  }

  override fun visitClassTypeStmt(stmt: Stmt.Type.Class): Descriptor {
    val name = stmt.name.lexeme
    val inherits = emptyList<KoflType>()
    val parameters = typedParameters(stmt.parameters)

    return ClassDescriptor(name, inherits, parameters)
  }

  private inline fun String.indexed(index: Int): String {
    val realIndex = if (index < 0) 0 else index

    return "$this-$realIndex"
  }

  private inline fun typedReturn(name: Token?): KoflType {
    return name?.lexeme
      ?.let { typeName -> findType(typeName) }
      ?: findType("Unit")
  }

  private inline fun typedParameters(parameters: Map<Token, Token>): Map<String, KoflType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findType(typeName.lexeme)
    }
  }

  private inline fun findType(name: String): KoflType {
    return container.peek().lookupType(name) ?: throw KoflCompileException.UnresolvedVar(name)
  }

  private inline fun <R> scoped(body: () -> R): R {
    container.push(TypeContainer(container.peek()))
    val value = body()
    container.pop()

    return value
  }
}