package com.lorenzoog.kofl.interpreter.typing

fun KoflType.isAssignableBy(another: KoflType?): Boolean {
  return this == KoflType.Primitive.Any || this == another
}

fun KoflType.isNumber(): Boolean {
  return this == KoflType.Primitive.Int || this == KoflType.Primitive.Double
}

class ClassBuilder internal constructor(
  private val name: String? = null,
  private val fields: MutableMap<String, KoflType> = mutableMapOf(),
  private val functions: MutableMap<String, List<KoflType.Function>> = mutableMapOf()
) {
  fun parameter(name: String, type: KoflType) {
    fields[name] = type
  }

  fun function(name: String, function: KoflType.Function) {
    val definedFunctions = functions[name] ?: emptyList()

    functions[name] = definedFunctions + function
  }

  fun build(): KoflType.Class = KoflType.Class(name, fields, functions)
}

fun createClass(name: String, builder: ClassBuilder.() -> Unit = {}): KoflType.Class {
  return ClassBuilder(name).apply(builder).build()
}