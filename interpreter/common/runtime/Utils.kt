package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.KoflCompileException
import com.lorenzoog.kofl.compiler.common.backend.FunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.NativeFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.typing.KoflType

class ClassBuilder internal constructor(private val definition: KoflType.Class) {
  private val constructors = mutableListOf<KoflObject.Callable>()
  private val fields = mutableMapOf<String, Value>()
  private val functions = mutableMapOf<String, List<KoflObject.Callable>>()

  fun function(descriptor: FunctionDescriptor, function: KoflObject.Callable) {
    val foundFunctions = functions[descriptor.name] ?: emptyList()

    functions[descriptor.name] = foundFunctions + function
  }

  fun constructor(function: KoflObject.Callable) {
    constructors += function
  }

  inline fun constructor(vararg parameters: Pair<String, KoflType>, noinline function: KoflNativeCallable) {
    constructor(mapOf(*parameters), function)
  }

  fun constructor(parameters: Map<String, KoflType>, function: KoflNativeCallable) {
    val descriptor = NativeFunctionDescriptor(
      name = definition.name ?: "<no name provided>",
      parameters = parameters,
      returnType = definition,
      nativeCall = "${definition.name}.<init>",
      line = -1
    )

    constructors += KoflObject.Callable.LocalNativeFunction(function, descriptor)
  }

  fun build(): KoflObject.Class {
    if (constructors.isEmpty()) {
      constructor(mapOf(), EMPTY_CONSTRUCTOR)
    }

    return KoflObject.Class(definition, constructors, functions)
  }
}

fun Environment.createClass(
  definition: KoflType.Class,
  builder: ClassBuilder.() -> Unit = {}
): KoflObject.Class {
  val name = definition.name ?: throw KoflCompileException.ClassMissingName(definition)
  val koflClass = ClassBuilder(definition).apply(builder).build()

  koflClass.constructors.forEachIndexed { index, constructor ->
    declareFunction("$name-$index", constructor)
  }

  return koflClass
}

fun Environment.createSingleton(definition: KoflType.Class, builder: ClassBuilder.() -> Unit = {}): KoflObject {
  val name = definition.name ?: throw KoflCompileException.ClassMissingName(definition)
  val koflClass = createClass(definition, builder)

  val createInstance = koflClass.constructors.first { callable -> callable.descriptor.parameters.isEmpty() }

  return createInstance(createInstance.descriptor, mapOf(), this).also { instance ->
    declare(name, Value.Immutable(instance))

    println("$this")
  }
}
