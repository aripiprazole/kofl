package com.lorenzoog.kofl.compiler.common.typing

import com.lorenzoog.kofl.compiler.common.KoflCompileException

data class TypeScope(
  private val enclosing: TypeScope? = null,
  private val types: MutableMap<String, KfType> = mutableMapOf(),
  private val variables: MutableMap<String, KfType> = mutableMapOf(),
  private val functions: MutableMap<String, List<KfType.Function>> = mutableMapOf()
) {
  fun containsName(name: String): Boolean {
    return types.containsKey(name) ||
      variables.containsKey(name) ||
      functions.containsKey(name)
  }

  fun defineType(name: String, type: KfType) {
    if (type is KfType.Function)
      defineFunction(name, type)

    if (type is KfType.Class)
      type.constructors.forEach {
        defineFunction(name, it)
      }

    types[name] = type
  }

  fun defineFunction(name: String, type: KfType.Function): Int {
    val alreadySigned = functions[name] ?: emptyList()

    functions[name] = alreadySigned + type

    return alreadySigned.indexOf(type)
  }

  fun define(name: String, type: KfType) {
    variables[name] = type
  }

  fun lookup(name: String): KfType {
    return variables[name]
      ?: enclosing?.lookup(name)
      ?: throw KoflCompileException.UnresolvedVar(name)
  }

  fun lookupFunctionOverload(name: String): List<KfType.Function> {
    return functions[name] ?: enclosing?.lookupFunctionOverload(name) ?: emptyList()
  }

  fun lookupType(name: String): KfType? {
    return types[name] ?: enclosing?.lookupType(name)
  }

  override fun toString(): String = (types + variables + functions).toString()
}
