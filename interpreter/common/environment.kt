package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Token

sealed class KoflValue {
  abstract val value: KoflObject

  data class Immutable(override val value: KoflObject) : KoflValue() {
    override fun toString(): String = value.toString()
  }

  data class Mutable(override var value: KoflObject) : KoflValue() {
    override fun toString(): String = value.toString()
  }
}

interface Environment {
  val enclosing: Environment? get() = null

  @KoflResolverInternals
  operator fun get(name: Token): KoflValue
}

interface MutableEnvironment : Environment {
  fun asMap(): Map<String, KoflValue>

  fun define(name: String, value: KoflValue)
  fun define(name: Token, value: KoflValue)

  @KoflResolverInternals
  fun getAt(distance: Int, name: Token): KoflValue

  @KoflResolverInternals
  fun setAt(distance: Int, name: Token, newValue: KoflObject)

  @KoflResolverInternals
  operator fun set(name: Token, newValue: KoflObject)
}

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
  } else throw IllegalOperationError(name, "define a variable that already exists")

  override fun define(name: Token, value: KoflValue) = define(name.lexeme, value)

  override fun getAt(distance: Int, name: Token): KoflValue {
    return ancestor(distance)[name]
  }

  override fun setAt(distance: Int, name: Token, newValue: KoflObject) {
    mutableAncestor(distance)[name] = newValue
  }

  override operator fun set(name: Token, newValue: KoflObject) = when (val value = this[name]) {
    is KoflValue.Immutable -> throw IllegalOperationError(name, "update an immutable variable")
    is KoflValue.Mutable -> value.value = newValue
  }

  override operator fun get(name: Token): KoflValue = values[name.lexeme]
    ?: enclosing?.get(name)
    ?: throw UnresolvedVarError(name)

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

fun Any.asKoflValue(mutable: Boolean = false): KoflValue {
  return asKoflObject().asKoflValue(mutable)
}

fun KoflObject.asKoflValue(mutable: Boolean): KoflValue {
  if (mutable) return KoflValue.Mutable(this)

  return KoflValue.Immutable(this)
}

@Suppress("DEPRECATION")
@Experimental(level = Experimental.Level.ERROR)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.LOCAL_VARIABLE,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.TYPEALIAS
)
@MustBeDocumented
annotation class KoflResolverInternals
