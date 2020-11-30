package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*
import com.lorenzoog.kofl.interpreter.backend.Compiler
import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.backend.Evaluator
import com.lorenzoog.kofl.interpreter.backend.KoflObject
import com.lorenzoog.kofl.interpreter.typing.KoflType
import com.lorenzoog.kofl.interpreter.typing.TypeContainer

const val MAX_STACK = 16

private val builtinTypeContainer = TypeContainer().apply {
  defineType("String", KoflType.Primitive.String)
  defineType("Int", KoflType.Primitive.Int)
  defineType("Double", KoflType.Primitive.Double)
  defineType("Unit", KoflType.Primitive.Unit)
}

interface Interpreter {
  val debug: Boolean
  val repl: Boolean

  fun lex(code: String): Collection<Token>
  fun parse(tokens: Collection<Token>): Collection<Stmt>
  fun compile(stmts: Collection<Stmt>): Collection<Descriptor>
  fun evaluate(descriptor: Descriptor): KoflObject
  fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject>

  companion object : Interpreter by Interpreter() {
    override fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject> {
      return Evaluator(mutableMapOf()).evaluate(descriptors)
    }
  }
}

fun Interpreter(debug: Boolean = false, repl: Boolean = false): Interpreter {
  return InterpreterImpl(debug, repl)
}

private class InterpreterImpl(override val debug: Boolean, override val repl: Boolean) : Interpreter {
  private val container = Stack<TypeContainer>(MAX_STACK).also { container ->
    container.push(builtinTypeContainer.copy())
  }
  private val locals = mutableMapOf<Descriptor, Int>()
  private val evaluator = Evaluator(locals)

  override fun lex(code: String): Collection<Token> {
    return Scanner(code).scan().also {
      if (debug) println("SCANNED: $it")
    }
  }

  override fun parse(tokens: Collection<Token>): Collection<Stmt> {
    return Parser(tokens.toList(), repl).parse().also {
      if (debug) println("PARSED: $it")
    }
  }

  override fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    return Compiler(locals, container).compile(stmts).also {
      if (debug) println("COMPILED: $it")
    }
  }

  override fun evaluate(descriptor: Descriptor): KoflObject {
    return evaluator.evaluate(descriptor)
  }

  override fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject> {
    return evaluator.evaluate(descriptors)
  }
}