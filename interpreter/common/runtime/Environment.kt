package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

sealed class Value {
  abstract val data: KoflObject

  class Immutable(override val data: KoflObject) : Value()
  class Mutable(override var data: KoflObject) : Value()
}

data class Environment(
  val callSite: Descriptor? = null,
  val enclosing: Environment? = null,
  val isGlobal: Boolean = false
) {
  private val expanded = mutableListOf<Environment>()
  private val variables = mutableMapOf<String, Value>()
  private val functions = mutableMapOf<String, KoflObject.Callable>()

  fun expand(module: Environment) {
    expanded += module
  }

  fun child(callSite: Descriptor, builder: Environment.() -> Unit = {}): Environment = copy(
    callSite = callSite,
    enclosing = this
  ).apply(builder)

  fun declareFunction(name: String, value: KoflObject.Callable) {
    if (functions.containsKey(name))
      throw KoflRuntimeException.AlreadyDeclaredVar(name, this)

    functions[name] = value
  }

  fun declare(name: String, value: Value) {
    if (variables.containsKey(name))
      throw KoflRuntimeException.AlreadyDeclaredVar(name, this)

    variables[name] = value
  }

  private fun lookupOrNull(name: String): KoflObject? {
    return variables[name]?.data
      ?: enclosing?.lookup(name)
      ?: expanded.fold(null as KoflObject?) { acc, environment ->
        acc ?: environment.lookupOrNull(name)
      }
  }

  fun lookup(name: String): KoflObject {
    return lookupOrNull(name) ?: throw KoflRuntimeException.UndefinedFunction(name, this)
  }

  private fun lookupFunctionOrNull(name: String): KoflObject.Callable? {
    return functions[name]
      ?: enclosing?.lookupFunction(name)
      ?: expanded.fold(null as KoflObject.Callable?) { acc, environment ->
        acc ?: environment.lookupFunctionOrNull(name)
      }
  }

  fun lookupFunction(name: String): KoflObject.Callable {
    return lookupFunctionOrNull(name) ?: throw KoflRuntimeException.UndefinedFunction(name, this)
  }

  fun assign(name: String, reassigned: KoflObject): Unit = when (val value = variables[name]) {
    null -> throw KoflRuntimeException.UndefinedVar(name, this)
    is Value.Immutable -> throw KoflRuntimeException.ReassignImmutableVar(name, this)
    is Value.Mutable -> value.data = reassigned
  }

  fun ancestor(distance: Int): Environment {
    var environment = this

    for (enclosing in 0..distance) {
      environment = environment.enclosing ?: continue
    }

    return environment
  }

  override fun toString(): String = buildString {
    append("Environment(")
    append("callSite=$callSite, ")
    append("enclosing=$enclosing, ")
    append("functions=$functions, ")
    append("variables=$variables")
    append(")")
  }
}
