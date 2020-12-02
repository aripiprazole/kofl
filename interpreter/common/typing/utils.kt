package com.lorenzoog.kofl.interpreter.typing

fun KoflType.isAssignableBy(another: KoflType?): Boolean {
  return this == KoflType.Any || this == another
}

fun KoflType.isNumber(): Boolean {
  return this == KoflType.Int || this == KoflType.Primitive.Double
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