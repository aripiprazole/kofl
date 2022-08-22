package me.devgabi.kofl.compiler.common.typing

infix fun KfType.isAssignableBy(another: KfType?): Boolean {
  return this == KfType.Any || this == another
}

fun KfType.isNumber(): Boolean {
  return this == KfType.Int || this == KfType.Double
}

class ClassDefinitionBuilder @PublishedApi internal constructor(name: String? = null) {
  private val constructors = mutableListOf<KfType.Function>()
  private val fields = mutableMapOf<String, KfType>()
  private val functions = mutableMapOf<String, List<KfType.Function>>()

  private val classDefinition = KfType.Class(name, constructors, fields, functions)

  fun constructor(vararg parameters: Pair<String, KfType>) {
    constructors += KfType.Function(mapOf(*parameters), classDefinition)
  }

  fun constructor(parameters: Map<String, KfType>) {
    constructors += KfType.Function(parameters, classDefinition)
  }

  fun parameter(name: String, type: KfType) {
    fields[name] = type
  }

  fun function(name: String, newFunctions: List<KfType.Function>) {
    functions[name] = newFunctions
  }

  fun function(name: String, function: KfType.Function) {
    val definedFunctions = functions[name] ?: emptyList()

    functions[name] = definedFunctions + function
  }

  fun build(): KfType.Class = classDefinition
}

inline fun createClassDefinition(
  name: String,
  builder: ClassDefinitionBuilder.() -> Unit = {}
): KfType.Class {
  return ClassDefinitionBuilder(name).apply(builder).build()
}

fun Collection<KfType.Callable>.match(
  vararg parameters: KfType,
  receiver: KfType? = null
): KfType.Callable? {
  return match(parameters.toList(), receiver)
}

fun Collection<KfType.Callable>.match(
  parameters: List<KfType>,
  receiver: KfType? = null
): KfType.Callable? {
  return firstOrNull { function ->
    val matchParameters = function.parameters.values.filterIndexed { i, parameterType ->
      parameterType.isAssignableBy(parameters.getOrNull(i))
    }

    matchParameters.size == parameters.size &&
      matchParameters.size == function.parameters.size &&
      (function as? KfType.Function)?.receiver == receiver
  }
}
