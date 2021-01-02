package com.lorenzoog.kofl.compiler.common.backend

import com.lorenzoog.kofl.compiler.common.KoflCompileException
import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.compiler.common.typing.TypeScope
import com.lorenzoog.kofl.compiler.common.typing.analyzer.DefaultTreeAnalyzer
import com.lorenzoog.kofl.compiler.common.typing.analyzer.TreeAnalyzer
import com.lorenzoog.kofl.compiler.common.typing.match
import com.lorenzoog.kofl.frontend.Expr
import com.lorenzoog.kofl.frontend.Stack
import com.lorenzoog.kofl.frontend.Stmt
import com.lorenzoog.kofl.frontend.Token
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class TreeDescriptorMapper(
  locals: MutableMap<Descriptor, Int>,
  private val container: Stack<TypeScope>,
  private val analyzer: TreeAnalyzer = DefaultTreeAnalyzer(locals, container)
) : Expr.Visitor<Descriptor>, Stmt.Visitor<Descriptor> {
  private val emitter = Emitter()

  fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    visitStmts(stmts).map {
      emitter.emit(it)
    }

    return emitter.compiled()
  }

  override fun visitAssignExpr(expr: Expr.Assign): Descriptor {
    val type = analyzer.analyze(expr)
    val name = expr.name.lexeme
    val value = visitExpr(expr.value)

    return AssignDescriptor(name, value, type, expr.line)
  }

  override fun visitBinaryExpr(expr: Expr.Binary): Descriptor {
    val type = analyzer.analyze(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return BinaryDescriptor(left, op, right, type, expr.line)
  }

  override fun visitLogicalExpr(expr: Expr.Logical): Descriptor {
    val type = analyzer.analyze(expr)
    val op = expr.op.type
    val left = visitExpr(expr.left)
    val right = visitExpr(expr.right)

    return LogicalDescriptor(left, op, right, type, expr.line)
  }

  override fun visitGroupingExpr(expr: Expr.Grouping): Descriptor {
    return visitExpr(expr.expr)
  }

  override fun visitLiteralExpr(expr: Expr.Literal): Descriptor {
    val type = analyzer.analyze(expr)

    return ConstDescriptor(expr.value, type, expr.line)
  }

  override fun visitUnaryExpr(expr: Expr.Unary): Descriptor {
    val type = analyzer.analyze(expr)
    val op = expr.op.type
    val right = visitExpr(expr.right)

    return UnaryDescriptor(op, right, type, expr.line)
  }

  override fun visitVarExpr(expr: Expr.Var): Descriptor {
    return AccessVarDescriptor(expr.name.lexeme, analyzer.analyze(expr), expr.line)
  }

  override fun visitCallExpr(expr: Expr.Call): Descriptor {
    val overload = analyzer.findOverload(expr.calle)
    val type = analyzer.findCallable(expr.calle, expr.arguments)

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

    return CallDescriptor(callee, arguments, type.returnType, expr.line)
  }

  override fun visitGetExpr(expr: Expr.Get): Descriptor {
    val type = analyzer.analyze(expr)
    val receiver = visitExpr(expr.receiver)
    val name = expr.name.lexeme

    return GetDescriptor(receiver, name, type, expr.line)
  }

  override fun visitSetExpr(expr: Expr.Set): Descriptor {
    val type = analyzer.analyze(expr)
    val receiver = visitExpr(expr.receiver)
    val value = visitExpr(expr.value)
    val name = expr.name.lexeme

    return SetDescriptor(receiver, name, value, type, expr.line)
  }

  override fun visitThisExpr(expr: Expr.ThisExpr): Descriptor {
    return ThisDescriptor(expr.line, KfType.Any)
  }

  override fun visitIfExpr(expr: Expr.IfExpr): Descriptor {
    val type = analyzer.analyze(expr)
    val condition = visitExpr(expr.condition)
    val then = visitStmts(expr.thenBranch)
    val orElse = visitStmts(expr.elseBranch ?: emptyList())

    return IfDescriptor(condition, then, orElse, type, expr.line)
  }

  override fun visitFuncExpr(expr: Expr.CommonFunc): Descriptor {
    analyzer.analyze(expr)

    val name = expr.name.lexeme
    val parameters = findParametersTypes(expr.parameters)
    val returnType = findReturnTypeByToken(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KfType.Function(parameters, returnType))

    return FunctionDescriptor(indexedName, parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body)
    }, expr.line, name)
  }

  override fun visitExtensionFuncExpr(expr: Expr.ExtensionFunc): Descriptor {
    analyzer.analyze(expr)

    val name = expr.name.lexeme
    val parameters = mapOf("this" to findTypeByName(expr.receiver.lexeme)) + findParametersTypes(expr.parameters)
    val returnType = findReturnTypeByToken(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))

    container.peek().defineFunction(name, KfType.Function(parameters, returnType))

    return FunctionDescriptor(name.indexed(index), parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body).let { body ->
        if (body.filterIsInstance<ReturnDescriptor>().none() && returnType == KfType.Unit)
        // add return if return is missing and return type is unit
          body + returnDescriptor {
            value = constDescriptor {
              value = Unit
              type = KfType.Unit
              line = expr.line
            }
            type = KfType.Unit
            line = expr.line
          }
        else body
      }
    }, expr.line, name)
  }

  override fun visitAnonymousFuncExpr(expr: Expr.AnonymousFunc): Descriptor {
    analyzer.analyze(expr)

    val parameters = findParametersTypes(expr.parameters)
    val returnType = findReturnTypeByToken(expr.returnType)

    return LocalFunctionDescriptor(parameters, returnType, scoped { container ->
      parameters.forEach { (name, type) ->
        container.define(name, type)
      }

      visitStmts(expr.body)
    }, expr.line)
  }

  override fun visitNativeFuncExpr(expr: Expr.NativeFunc): Descriptor {
    analyzer.analyze(expr)

    val name = expr.name.lexeme
    val parameters = findParametersTypes(expr.parameters)
    val returnType = findReturnTypeByToken(expr.returnType)
    val overload = container.peek().lookupFunctionOverload(expr.name.lexeme)
    val index = overload.indexOf(overload.match(parameters.values.toList()))
    val indexedName = name.indexed(index)

    container.peek().defineFunction(name, KfType.Function(parameters, returnType))

    return NativeFunctionDescriptor(indexedName, parameters, returnType, name, expr.line, name)
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
    analyzer.validate(stmt)

    val value = visitExpr(stmt.expr)

    return ReturnDescriptor(value, analyzer.analyze(stmt.expr), stmt.line)
  }

  override fun visitValDeclStmt(stmt: Stmt.ValDecl): Descriptor {
    analyzer.validate(stmt)

    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return ValDescriptor(name, value, analyzer.analyze(stmt.value), stmt.line)
  }

  override fun visitVarDeclStmt(stmt: Stmt.VarDecl): Descriptor {
    analyzer.validate(stmt)

    val name = stmt.name.lexeme
    val value = visitExpr(stmt.value)

    return VarDescriptor(name, value, analyzer.analyze(stmt.value), stmt.line)
  }

  override fun visitTypeRecordStmt(stmt: Stmt.Type.Record): Descriptor {
    val name = stmt.name.lexeme
    val inherits = emptyList<KfType>()
    val parameters = findParametersTypes(stmt.parameters)

    return ClassDescriptor(name, inherits, parameters, stmt.line)
  }

  private inline fun String.indexed(index: Int): String {
    val realIndex = if (index < 0) 0 else index

    return "$this-$realIndex"
  }

  private inline fun findReturnTypeByToken(name: Token?): KfType {
    return name?.lexeme
      ?.let { typeName -> findTypeByName(typeName) }
      ?: KfType.Unit
  }

  private inline fun findParametersTypes(parameters: Map<Token, Token>): Map<String, KfType> {
    return parameters.mapKeys { (name) -> name.lexeme }.mapValues { (_, typeName) ->
      findTypeByName(typeName.lexeme)
    }
  }

  private inline fun findTypeByName(name: String): KfType {
    return container.peek().lookupType(name) ?: throw KoflCompileException.UnresolvedVar(name)
  }

  @OptIn(ExperimentalContracts::class)
  private inline fun <R> scoped(body: (TypeScope) -> R): R {
    contract {
      callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }

    container.push(TypeScope(container.peek()))
    val value = body(container.peek())
    container.pop()

    return value
  }

  override fun visitUseStmt(stmt: Stmt.UseDecl): Descriptor {
    analyzer.validate(stmt)

    return UseDescriptor(stmt.module.lexeme, stmt.line)
  }

  override fun visitModuleStmt(stmt: Stmt.ModuleDecl): Descriptor {
    analyzer.validate(stmt)

    return ModuleDescriptor(stmt.module.lexeme, stmt.line)
  }
}