package me.devgabi.kofl.interpreter.module

import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.backend.NativeDescriptor
import me.devgabi.kofl.compiler.common.typing.KfType
import me.devgabi.kofl.compiler.common.typing.isAssignableBy
import me.devgabi.kofl.interpreter.runtime.Evaluator
import me.devgabi.kofl.interpreter.runtime.KoflObject

class MainNotFoundException internal constructor() : Error(
  "Main not found in programing, exiting 1"
)

class MainReturnedNotInt internal constructor() : Error("Main returned other thing that isn't Int")

class SourceCode(
  private val repl: Boolean,
  private val evaluator: Evaluator,
  private val descriptors: Collection<Descriptor>,
  private val debug: Boolean = false
) {
  private var objects = mutableListOf<KoflObject>()

  private fun initRuntime() {
    objects.addAll(evaluator.evaluate(descriptors))
  }

  /**
   * Creates a new source code with the same config but with new code
   */
  infix fun merge(another: SourceCode): SourceCode {
    return SourceCode(repl, evaluator, descriptors + another.descriptors)
  }

  /**
   * Will execute the provided KOFL code
   */
  fun main(args: Array<String>): Int {
    // TODO handle args

    if (debug) println("OBJECTS: $objects")

    initRuntime()

    if (repl) return 0

    val mainFunc = objects
      .filterIsInstance<KoflObject.Callable.Function>()
      .filter { it.descriptor.simpleName == "main" }
      .firstOrNull { it.descriptor.returnType.isAssignableBy(KfType.Int) }
      ?: throw MainNotFoundException()

    return mainFunc(NativeDescriptor, mapOf(), evaluator.globalEnvironment).unwrap().also {
      if (it !is Int) throw MainReturnedNotInt()
    } as Int
  }
}
