package com.lorenzoog.kofl.interpreter.typing

import com.lorenzoog.kofl.interpreter.exceptions.KoflCompileException

data class TypeContainer(
  private val enclosing: TypeContainer? = null,
  private val types: MutableMap<String, KoflType> = mutableMapOf(),
  private val variables: MutableMap<String, KoflType> = mutableMapOf(),
  private val functions: MutableMap<String, List<KoflType.Function>> = mutableMapOf()
) {
  fun containsName(name: String): Boolean {
    return types.containsKey(name)
      || variables.containsKey(name)
      || functions.containsKey(name)
  }

  fun defineType(name: String, type: KoflType) {
    if (type is KoflType.Function)
      defineFunction(name, type)

    if (type is KoflType.Class)
      type.constructors.forEach {
        defineFunction(name, it)
      }

    types[name] = type
  }

  fun defineFunction(name: String, type: KoflType.Function): Int {
    val alreadySigned = functions[name] ?: emptyList()

    functions[name] = alreadySigned + type

    return alreadySigned.indexOf(type)
  }

  fun define(name: String, type: KoflType) {
    variables[name] = type
  }

  fun lookup(name: String): KoflType {
    return variables[name]
      ?: enclosing?.lookup(name)
      ?: throw KoflCompileException.UnresolvedVar(name)
  }

  fun lookupFunctionOverload(name: String): List<KoflType.Function> {
    return functions[name] ?: enclosing?.lookupFunctionOverload(name) ?: emptyList()
  }

  fun lookupType(name: String): KoflType? {
    return types[name] ?: enclosing?.lookupType(name)
  }

  override fun toString(): String = (types + variables + functions).toString()
}
