package com.lorenzoog.kofl.interpreter.typing

import com.lorenzoog.kofl.interpreter.exceptions.KoflCompileTimeException

data class TypeContainer(private val enclosing: TypeContainer? = null) {
  private val types = mutableMapOf<String, KoflType>()
  private val variables = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<String, List<KoflType.Function>>()

  fun defineType(name: String, type: KoflType) {
    if (type is KoflType.Function)
      defineFunc(name, type)

    types[name] = type
  }

  fun defineFunc(name: String, type: KoflType.Function) {
    val alreadySigned = functions[name] ?: emptyList()

    functions[name] = alreadySigned + type
  }

  fun define(name: String, type: KoflType) {
    variables[name] = type
  }

  fun lookup(name: String): KoflType {
    return variables[name]
      ?: enclosing?.lookup(name)
      ?: throw KoflCompileTimeException.UnresolvedVar(name)
  }

  fun lookupFuncOverload(name: String): List<KoflType.Function> {
    return functions[name] ?: emptyList()
  }

  fun lookupType(name: String): KoflType? {
    return types[name] ?: enclosing?.lookupType(name)
  }

  override fun toString(): String = (types + variables + functions).toString()
}

inline fun Collection<KoflType.Function>.match(
  vararg parameters: KoflType,
  receiver: KoflType? = null
): KoflType.Function? {
  return match(parameters.toList(), receiver)
}

inline fun Collection<KoflType.Function>.match(
  parameters: List<KoflType>,
  receiver: KoflType? = null
): KoflType.Function? {
  return firstOrNull { function ->
    val matchParameters = function.parameters.values.filterIndexed { i, parameterType ->
      parameterType.isAssignableBy(parameters.getOrNull(i))
    }

    matchParameters.size == parameters.size && function.receiver == receiver
  }
}
