package com.lorenzoog.kofl.compiler.kvm.typing

fun KoflType.isAssignableBy(another: KoflType?): Boolean {
  return this == KoflType.Any || this == another
}

fun KoflType.isNumber(): Boolean {
  return this == KoflType.Int || this == KoflType.Double
}

class ClassDefinitionBuilder internal constructor(name: String? = null) {
  private val constructors = mutableListOf<KoflType.Function>()
  private val fields = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<String, List<KoflType.Function>>()

  private val classDefinition = KoflType.Class(name, constructors, fields, functions)

  fun constructor(vararg parameters: Pair<String, KoflType>) {
    constructors += KoflType.Function(mapOf(*parameters), classDefinition)
  }

  fun constructor(parameters: Map<String, KoflType>) {
    constructors += KoflType.Function(parameters, classDefinition)
  }

  fun parameter(name: String, type: KoflType) {
    fields[name] = type
  }

  fun function(name: String, newFunctions: List<KoflType.Function>) {
    functions[name] = newFunctions
  }

  fun function(name: String, function: KoflType.Function) {
    val definedFunctions = functions[name] ?: emptyList()

    functions[name] = definedFunctions + function
  }

  fun build(): KoflType.Class = classDefinition
}

fun createClassDefinition(name: String, builder: ClassDefinitionBuilder.() -> Unit = {}): KoflType.Class {
  return ClassDefinitionBuilder(name).apply(builder).build()
}

inline fun Collection<KoflType.Callable>.match(
  vararg parameters: KoflType,
  receiver: KoflType? = null
): KoflType.Callable? {
  return match(parameters.toList(), receiver)
}

inline fun Collection<KoflType.Callable>.match(
  parameters: List<KoflType>,
  receiver: KoflType? = null
): KoflType.Callable? {
  return firstOrNull { function ->
    val matchParameters = function.parameters.values.filterIndexed { i, parameterType ->
      parameterType.isAssignableBy(parameters.getOrNull(i))
    }

    matchParameters.size == parameters.size
      && matchParameters.size == function.parameters.size
      && (function as? KoflType.Function)?.receiver == receiver
  }
}
