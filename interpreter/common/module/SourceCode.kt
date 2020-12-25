package com.lorenzoog.kofl.interpreter.module

import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.backend.NativeDescriptor
import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.compiler.common.typing.isAssignableBy
import com.lorenzoog.kofl.interpreter.runtime.Evaluator
import com.lorenzoog.kofl.interpreter.runtime.KoflObject

class MainNotFoundException internal constructor() : Error("Main not found in programing, exiting 1")
class MainReturnedNotInt internal constructor() : Error("Main returned other thing that isn't Int")

class SourceCode(
  private val repl: Boolean,
  private val evaluator: Evaluator,
  private val descriptors: Collection<Descriptor>
) {
  private var objects = mutableListOf<KoflObject>()

  private fun initRuntime() {
    objects.addAll(evaluator.evaluate(descriptors))
  }

  /**
   * Will execute the provided KOFL code
   */
  fun main(args: Array<String>): Int {
    // TODO handle args

    initRuntime()

    if(repl) return 0

    val mainFunc = objects
      .filterIsInstance<KoflObject.Callable.Function>()
      .filter { it.descriptor.name == "main" }
      .firstOrNull { it.descriptor.returnType.isAssignableBy(KoflType.Int) }
      ?: throw MainNotFoundException()

    return mainFunc(NativeDescriptor, mapOf(), evaluator.globalEnvironment).unwrap().also {
      if (it !is Int) throw MainReturnedNotInt()
    } as Int
  }

}