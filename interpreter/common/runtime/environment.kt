package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.interpreter.backend.Descriptor
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

sealed class Value {
  abstract val data: KoflObject

  class Immutable(override val data: KoflObject) : Value()
  class Mutable(override var data: KoflObject) : Value()
}

data class Environment(val callSite: Descriptor? = null, val enclosing: Environment? = null) {
  private val variables = mutableMapOf<String, Value>()
  private val functions = mutableMapOf<String, KoflObject.Callable>()

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

  fun lookup(name: String): KoflObject {
    return variables[name]?.data
      ?: enclosing?.lookup(name)
      ?: throw KoflRuntimeException.UndefinedVar(name, this)
  }

  fun lookupFunction(name: String): KoflObject.Callable {
    return functions[name]
      ?: enclosing?.lookupFunction(name)
      ?: throw KoflRuntimeException.UndefinedFunction(name, this)
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

  override fun toString(): String = "Environment(callSite=$callSite, enclosing=$enclosing, functions=$functions, variables=$variables)"
}