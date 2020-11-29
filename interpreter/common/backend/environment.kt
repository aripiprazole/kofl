package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

sealed class Value {
  abstract val data: KoflObject

  class Immutable(override val data: KoflObject) : Value()
  class Mutable(override var data: KoflObject) : Value()
}

data class Environment(val enclosing: Environment? = null) {
  private val variables = mutableMapOf<String, Value>()
  private val functions = mutableMapOf<String, KoflObject.Callable>()

  fun child(builder: Environment.() -> Unit): Environment = copy(enclosing = this).apply(builder)

  fun lookupAt(distance: Int, name: String): KoflObject {
    return ancestor(distance).lookup(name)
  }

  fun assignAt(distance: Int, name: String, data: KoflObject) {
    return ancestor(distance).assign(name, data)
  }

  fun declareFunction(name: String, value: KoflObject.Callable) {
    if (functions.containsKey(name))
      throw KoflRuntimeException.AlreadyDeclaredVar(name)

    functions[name] = value
  }

  fun declare(name: String, value: Value) {
    if (variables.containsKey(name))
      throw KoflRuntimeException.AlreadyDeclaredVar(name)

    variables[name] = value
  }

  fun lookup(name: String): KoflObject {
    return variables[name]?.data ?: throw KoflRuntimeException.AlreadyDeclaredVar(name)
  }

  fun assign(name: String, reassigned: KoflObject): Unit = when (val value = variables[name]) {
    null -> throw KoflRuntimeException.UndefinedVar(name)
    is Value.Immutable -> throw KoflRuntimeException.ReassignImmutableVar(name)
    is Value.Mutable -> value.data = reassigned
  }

  private fun ancestor(distance: Int): Environment {
    var environment = this

    for (enclosing in 0..distance) {
      environment = environment.enclosing ?: continue
    }

    return environment
  }
}