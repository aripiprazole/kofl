package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Stack


class TypeEnvironment(private val enclosing: TypeEnvironment? = null) {
  private val types = mutableMapOf<String, KoflType>()
  private val variables = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<String, List<KoflCallable>>()

  fun defineName(name: String, type: KoflType) {
    variables[name] = type
  }

  fun defineFunction(name: String, type: KoflCallable) {
    val alreadySigned = functions[name] ?: emptyList()

    functions[name] = alreadySigned + type
  }

  fun findFunction(name: String): List<KoflCallable> {
    return functions[name] ?: emptyList()
  }

  fun findName(name: String): KoflType {
    return variables[name]
      ?: enclosing?.findName(name)
      ?: throw UnresolvedVarException(name)
  }

  fun findTypeOrNull(name: String): KoflType? {
    return types[name] ?: enclosing?.findTypeOrNull(name)
  }

  fun findType(name: String): KoflType {
    return types[name] ?: enclosing?.findType(name) ?: throw UnresolvedVarException(name)
  }

  fun defineType(name: String, type: KoflType) {
    if (type is KoflCallable)
      defineFunction(name, type)
    types[name] = type
  }

  override fun toString(): String = (types + variables + functions).toString()
}

fun globalEnvironment(size: Int, builder: TypeEnvironment.() -> Unit): Stack<TypeEnvironment> {
  return Stack<TypeEnvironment>(size).apply {
    push(TypeEnvironment().apply(builder))
  }
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
