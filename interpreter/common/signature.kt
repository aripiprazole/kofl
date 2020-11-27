package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Stack


class TypeEnvironment(private val enclosing: TypeEnvironment? = null) {
  private val types = mutableMapOf<String, KoflType>()
  private val variables = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<String, List<KoflCallable>>()

  fun defineType(name: String, type: KoflType) {
    if (type is KoflCallable)
      defineFunc(name, type)

    types[name] = type
  }

  fun defineFunc(name: String, type: KoflCallable) {
    val alreadySigned = functions[name] ?: emptyList()

    functions[name] = alreadySigned + type
  }

  fun define(name: String, type: KoflType) {
    variables[name] = type
  }

  fun lookup(name: String): KoflType {
    return variables[name]
      ?: enclosing?.lookup(name)
      ?: throw UnresolvedVarException(name)
  }

  fun lookupFuncOverload(name: String): List<KoflCallable> {
    return functions[name] ?: emptyList()
  }

  fun lookupTypeOrNull(name: String): KoflType? {
    return types[name] ?: enclosing?.lookupTypeOrNull(name)
  }

  inline fun lookupType(name: String): KoflType {
    return lookupTypeOrNull(name) ?: throw UnresolvedVarException(name)
  }

  override fun toString(): String = (types + variables + functions).toString()
}

inline fun Collection<KoflCallable>.match(vararg parameters: KoflType, receiver: KoflType? = null): KoflCallable? {
  return match(parameters.toList(), receiver)
}

inline fun Collection<KoflCallable>.match(parameters: Collection<KoflType>, receiver: KoflType? = null): KoflCallable? {
  return firstOrNull { type ->
    type.parameters.values.containsAll(parameters) && if (type is ExtensionFunc) {
      type.receiver == receiver
    } else true
  }
}
