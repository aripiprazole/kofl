package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*
import com.lorenzoog.kofl.interpreter.backend.Compiler
import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.backend.Evaluator
import com.lorenzoog.kofl.interpreter.backend.KoflObject
import com.lorenzoog.kofl.interpreter.typing.TypeContainer

const val MAX_STACK = 16

private val builtinTypeContainer = TypeContainer()

interface Interpreter {
  val debug: Boolean
  val repl: Boolean

  fun lex(code: String): Collection<Token>
  fun parse(tokens: Collection<Token>): Collection<Stmt>
  fun compile(stmts: Collection<Stmt>): Collection<Descriptor>
  fun evaluate(descriptor: Descriptor): KoflObject
  fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject>

  companion object : Interpreter by Interpreter() {
    override fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
      return Compiler(Stack<TypeContainer>(MAX_STACK).also { container ->
        container.push(builtinTypeContainer.copy())
      }).compile(stmts)
    }

    override fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject> {
      return Evaluator().evaluate(descriptors)
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
  private val evaluator = Evaluator()
  private val compiler = Compiler(container)

  override fun lex(code: String): Collection<Token> {
    return Scanner(code).scan()
  }

  override fun parse(tokens: Collection<Token>): Collection<Stmt> {
    return Parser(tokens.toList()).parse()
  }

  override fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    return compiler.compile(stmts)
  }

  override fun evaluate(descriptor: Descriptor): KoflObject {
    return evaluator.evaluate(descriptor)
  }

  override fun evaluate(descriptors: Collection<Descriptor>): Collection<KoflObject> {
    return evaluator.evaluate(descriptors)
  }
}