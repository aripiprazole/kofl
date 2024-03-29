package me.devgabi.kofl.interpreter

import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.backend.TreeDescriptorMapper
import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.compiler.common.typing.TypeScope
import me.devgabi.kofl.frontend.Parser
import me.devgabi.kofl.frontend.Stack
import me.devgabi.kofl.frontend.Stmt
import me.devgabi.kofl.interpreter.module.SourceCode
import me.devgabi.kofl.interpreter.runtime.Evaluator
import me.devgabi.kofl.interpreter.runtime.KoflObject
import kotlin.contracts.ExperimentalContracts

const val MAX_STACK = 16

private val builtinTypeContainer = TypeScope().apply {
  defineType("Any", KfType.Any)
  defineType("String", KfType.String)
  defineType("Int", KfType.Int)
  defineType("Double", KfType.Double)
  defineType("Boolean", KfType.Boolean)
  defineType("Unit", KfType.Unit)
}

interface Interpreter {
  val debug: Boolean
  val repl: Boolean

  fun parse(code: String): Collection<Stmt>
  fun compile(stmts: Collection<Stmt>): Collection<Descriptor>
  fun evaluate(descriptor: Descriptor): KoflObject
  fun evaluate(descriptors: Collection<Descriptor>): SourceCode

  companion object : Interpreter by Interpreter() {
    override fun evaluate(descriptors: Collection<Descriptor>): SourceCode {
      return SourceCode(
        repl = false,
        evaluator = Evaluator(mutableMapOf()),
        descriptors = descriptors
      )
    }
  }
}

fun Interpreter.execute(code: String): SourceCode {
  val stmts = parse(code)
  val descriptors = compile(stmts)

  return evaluate(descriptors)
}

fun Interpreter(
  debug: Boolean = false,
  repl: Boolean = false,
  logger: Logger? = null
): Interpreter {
  return InterpreterImpl(debug, repl, logger)
}

private class InterpreterImpl(
  override val debug: Boolean,
  override val repl: Boolean,
  private val logger: Logger?
) : Interpreter {
  private val container = Stack<TypeScope>(MAX_STACK).also { container ->
    container.push(builtinTypeContainer.copy())
  }
  private val locals = linkedMapOf<Descriptor, Int>()
  private val evaluator = Evaluator(locals)

  override fun parse(code: String): Collection<Stmt> {
    return Parser(code, repl).parse().also {
      if (debug) logger?.trace("PARSED: $it")
    }
  }

  @ExperimentalContracts
  override fun compile(stmts: Collection<Stmt>): Collection<Descriptor> {
    return TreeDescriptorMapper(locals, container).compile(stmts).also {
      if (debug) logger?.trace("COMPILED: $it")
    }
  }

  override fun evaluate(descriptor: Descriptor): KoflObject {
    return evaluator.evaluate(descriptor)
  }

  override fun evaluate(descriptors: Collection<Descriptor>): SourceCode {
    return SourceCode(repl, evaluator, descriptors, debug)
  }
}
