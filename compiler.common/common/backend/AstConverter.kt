package com.lorenzoog.kofl.compiler.common.backend

import com.lorenzoog.kofl.compiler.common.KoflCompileException
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.TypeContainer
import com.lorenzoog.kofl.compiler.common.typing.TypeValidator
import com.lorenzoog.kofl.compiler.common.typing.match
import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stack
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class AstConverter(
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

    return AssignDescriptor(name, value, type, expr.line)
  }

  override fun visitBinaryExpr(expr: Expr.Binary): Descriptor {
    val type = validator.visitBinaryExpr(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return BinaryDescriptor(left, op, right, type, expr.line)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): Descriptor {
    val type = validator.visitLogicalExpr(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return LogicalDescriptor(left, op, right, type, expr.line)
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): Descriptor {
    return visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): Descriptor {
    val type = validator.visitLiteralExpr(expr)

    return ConstDescriptor(expr.value, type, expr.line)
  }

  override fun visitUnaryExpr(expr: Expr.Unary): Descriptor {
    val type = validator.visitUnaryExpr(expr)
    val op = expr.op.type
    val right = visitExpr(expr.right)

    return UnaryDescriptor(op, right, type, expr.line)
  }

  override fun visitVarExpr(expr: Expr.Var): Descriptor {
    return AccessVarDescriptor(expr.name.lexeme, validator.visitVarExpr(expr), expr.line)
  }

  override fun visitCallExpr(expr: Expr.Call): Descriptor {
    val overload = validator.findCallOverload(expr.calle)
    val type = validator.findCallCallee(expr.calle, expr.arguments)
    val returnType = validator.visitCallExpr(expr)

    val index = overload.indexOf(type)

    val arguments = mapOf(*expr.arguments.entries.mapIndexed { i, (_, expr) ->
      val name = type.parameters.entries.toList().getOrNull(i)?.key
        ?: throw KoflCompileException.UnresolvedParameter(i)

      val descriptor = visitExpr(expr)

      name to descriptor
    }.toTypedArray())

    val callee = when (val callee = expr.calle) {
      is Expr.Var -> {
        val name = callee.name.lexeme

        if (overload.isNotEmpty()) {
          if (overload.match(arguments.values.map { it.type }) == null)
            throw KoflCompileException.UnresolvedFunction(name)

          AccessFunctionDescriptor("$name-$index", type, expr.line)
        } else AccessVarDescriptor(name, type, expr.line)
      }
      else -> visitExpr(expr.calle)
    }

    return CallDescriptor(callee, arguments, returnType, expr.line)
  }

  override fun visitGetExpr(expr: Expr.Get): Descriptor {
    val type = validator.visitGetExpr(expr)
    val receiver = visitExpr(expr.receiver)
    val name = expr.name.lexeme

    return GetDescriptor(receiver, name, type, expr.line)
  }

  override fun visitSetExpr(expr: Expr.Set): Descriptor {
    val type = validator.visitSetExpr(expr)
    val receiver = visitExpr(expr.receiver)
    val value = visitExpr(expr.value)
    val name = expr.name.lexeme

    return SetDescriptor(receiver, name, value, type, expr.line)
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): Descriptor {
    return ThisDescriptor(expr.line, KoflType.Any)
  }

  override fun visitIfExpr(expr: Expr.IfExpr): Descriptor {
    val type = validator.visitIfExpr(expr)
    val condition = visitExpr(expr.condition)
    val then = visitStmts(expr.thenBranch)
    val orElse = visitStmts(expr.elseBranch ?: emptyList())

    return IfDescriptor(condition, then, orElse, type, expr.line)
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): Descriptor {
    validator.visitFuncExpr(expr)

    val name = expr.name.lexeme
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return FunctionDescriptor(indexedName, parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body)
    }, expr.line)
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): Descriptor {
    validator.visitExtensionFuncExpr(expr)

    val name = expr.name.lexeme
    val parameters = mapOf("this" to findType(expr.receiver.lexeme)) + typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return FunctionDescriptor(name.indexed(index), parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body).let { body ->
        if (body.filterIsInstance<ReturnDescriptor>().none() && returnType == KoflType.Unit)
        // add return if return is missing and return type is unit
          body + ReturnDescriptor(ConstDescriptor(Unit, KoflType.Unit, expr.line), KoflType.Unit, expr.line)
        else body
      }
    }, expr.line)
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): Descriptor {
    validator.visitAnonymousFuncExpr(expr)

    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)

    return LocalFunctionDescriptor(parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body)
    }, expr.line)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): Descriptor {
    validator.visitNativeFuncExpr(expr)

    val name = expr.name.lexeme
    val parameters = typedParameters(expr.parameters)
    val returnType = typedReturn(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KoflType.Function(parameters, returnType))

    return NativeFunctionDescriptor(indexedName, parameters, returnType, name, expr.line)
  }

  override fun visitExprStmt(stmt: Stmt.ExprStmt): Descriptor {
    return visitExpr(stmt.expr)
  }

  override fun visitBlockStmt(stmt: Stmt.Block): Descriptor {
    return BlockDescriptor(scoped {
      visitStmts(stmt.body)
    }, stmt.line)
  }

  override fun visitWhileStmt(stmt: Stmt.WhileStmt): Descriptor {
    val condition = visitExpr(stmt.condition)

    return WhileDescriptor(condition, scoped {
      visitStmts(stmt.body)
    }, stmt.line)
  }

  override fun visitReturnStmt(stmt: Stmt.ReturnStmt): Descriptor {
    val type = validator.visitReturnStmt(stmt)
    val value = visitExpr(stmt.expr)

    return ReturnDescriptor(value, type, stmt.line)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): Descriptor {
    val type = validator.visitValDeclStmt(stmt)
    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return ValDescriptor(name, value, type, stmt.line)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): Descriptor {
    val type = validator.visitVarDeclStmt(stmt)
    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return VarDescriptor(name, value, type, stmt.line)
  }

  override fun visitClassTypeStmt(stmt: Stmt.Type.Class): Descriptor {
    val name = stmt.name.lexeme
    val inherits = emptyList<KoflType>()
    val parameters = typedParameters(stmt.parameters)

    return ClassDescriptor(name, inherits, parameters, stmt.line)
  }

  private inline fun String.indexed(index: Int): String {
    val realIndex = if (index < 0) 0 else index

    return "$this-$realIndex"
  }

  private inline fun typedReturn(name: Token?): KoflType {
    return name?.lexeme
      ?.let { typeName -> findType(typeName) }
      ?: KoflType.Unit
  }

  private inline fun typedParameters(parameters: Map<Token, Token>): Map<String, KoflType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findType(typeName.lexeme)
    }
  }

  private inline fun findType(name: String): KoflType {
    return container.peek().lookupType(name) ?: throw KoflCompileException.UnresolvedVar(name)
  }

  @OptIn(ExperimentalContracts::class)
  private inline fun <R> scoped(body: (TypeContainer) -> R): R {
    contract {
      callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    container.push(TypeContainer(container.peek()))
    val value = body(container.peek())
    container.pop()

    return value
  }

  override fun visitUseStmt(stmt: Stmt.UseDecl): Descriptor {
    validator.visitUseStmt(stmt)

    return UseDescriptor(stmt.module.lexeme, stmt.line)
  }

  override fun visitModuleStmt(stmt: Stmt.ModuleDecl): Descriptor {
    validator.visitModuleStmt(stmt)

    return ModuleDescriptor(stmt.module.lexeme, stmt.line)
  }
}