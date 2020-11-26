package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

@OptIn(KoflResolverInternals::class)
fun MutableEnvironment(enclosing: Environment? = null): MutableEnvironment {
  return KoflEnvironment(enclosing)
}

@KoflResolverInternals
private class KoflEnvironment(override val enclosing: Environment? = null) : MutableEnvironment {
  private val values = mutableMapOf<String, KoflValue>()

  override fun asMap(): Map<String, KoflValue> {
    return values
  }

  override fun define(name: String, value: KoflValue) = if (values[name] == null) {
    values[name] = value
  } else throw IllegalOperationException(name, "define a variable that already exists")

  override fun define(name: Token, value: KoflValue) = define(name.lexeme, value)

  override fun getAt(distance: Int, name: Token): KoflValue {
    return ancestor(distance)[name]
  }

  override fun setAt(distance: Int, name: Token, newValue: KoflObject) {
    mutableAncestor(distance)[name] = newValue
  }

  override operator fun set(name: Token, newValue: KoflObject) = when (val value = this[name]) {
    is KoflValue.Immutable -> throw IllegalOperationException(name, "update an immutable variable")
    is KoflValue.Mutable -> value.value = newValue
  }

  override operator fun get(name: Token): KoflValue = values[name.lexeme]
    ?: enclosing?.get(name)
    ?: throw UnresolvedVarException(name)

  override fun toString(): String = "KoflEnvironment(enclosing=$enclosing, values=$values)"

  // utils
  private fun ancestor(distance: Int): Environment {
    var environment: Environment = this

    for (index in 0 until distance) {
      environment = environment.enclosing ?: continue
    }

    return environment
  }

  private fun mutableAncestor(distance: Int): MutableEnvironment {
    var environment: MutableEnvironment = this

    for (index in 0..distance) {
      environment = (environment.enclosing as? MutableEnvironment) ?: continue
    }

    return environment
  }
}
