package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*
import com.lorenzoog.kofl.interpreter.backend.Compiler
import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.runtime.Evaluator
import com.lorenzoog.kofl.interpreter.runtime.KoflObject
import com.lorenzoog.kofl.interpreter.typing.KoflType
import com.lorenzoog.kofl.interpreter.typing.TypeContainer

const val MAX_STACK = 16

private val builtinTypeContainer = TypeContainer().apply {
  defineType("String", KoflType.String)
  defineType("Int", KoflType.Int)
  defineType("Double", KoflType.Double)
  defineType("Unit", KoflType.Unit)
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

inline fun Interpreter.execute(code: String): Collection<KoflObject> {
  val tokens = lex(code)
  val stmts = parse(tokens)
  val descriptors = compile(stmts)

  return evaluate(descriptors)
}

fun Interpreter(debug: Boolean = false, repl: Boolean = false, consoleSender: ConsoleSender? = null): Interpreter {
  return InterpreterImpl(debug, repl, consoleSender)
}

private class InterpreterImpl(
  override val debug: Boolean,
  override val repl: Boolean,
  private val consoleSender: ConsoleSender?
) : Interpreter {
  private val container = Stack<TypeContainer>(MAX_STACK).also { container ->
    container.push(builtinTypeContainer.copy())
  }
  private val locals = linkedMapOf<Descriptor, Int>()
  private val evaluator = Evaluator(locals)

  override fun lex(code: String): Collection<Token> {
    return Scanner(code).scan().also {
      if (debug) consoleSender?.trace("SCANNED: $it")
    }
  }

  override fun parse(tokens: Collection<Token>): Collection<Stmt> {
    return Parser(tokens.toList(), repl).parse().also {
      if (debug) consoleSender?.trace("PARSED: $it")
    }
  }

  override fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    return Compiler(locals, container).compile(stmts).also {
      if (debug) consoleSender?.trace("COMPILED: $it")
    }
  }

  override fun evaluate(descriptor: Descriptor): KoflObject {
    return evaluator.evaluate(descriptor)
  }

  override fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject> {
    return evaluator.evaluate(descriptors)
  }
}