package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.InvalidDeclaredTypeException
import com.lorenzoog.kofl.frontend.InvalidTypeException
import com.lorenzoog.kofl.frontend.Token
import kotlin.reflect.KClass

@Suppress("DEPRECATION")
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

interface KoflType

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

sealed class KoflValue(val type: KoflType) {
  abstract val value: KoflObject

  class Immutable(override val value: KoflObject, type: KoflType) : KoflValue(type)
  class Mutable(override var value: KoflObject, type: KoflType) : KoflValue(type)

  override fun toString(): String = value.toString()
}

data class KoflStruct(
  val name: String,
  val fields: Map<String, KoflType>
) : KoflCallable(fields, KoflInstance), KoflType {
  override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    val fieldsList = fields.entries.toList()
    val fields = mutableMapOf<String, KoflValue>()

    arguments.entries.forEachIndexed { i, argument ->
      fields[fieldsList[i].key] = argument.value.asKoflValue()
    }

    return KoflInstance(this, fields)
  }

  override fun toString(): String = "struct $name"
}

abstract class KoflSingleton : KoflObject(), KoflType {
  abstract override fun toString(): String
}

abstract class KoflPrimitive<T : Any>(
  private val kClass: KClass<T>,
  parameterType: KoflType,
  returnType: KoflType,
) : KoflCallable(mapOf("raw" to parameterType), returnType), KoflType {
  override fun toString(): String = "<primitive ${kClass.simpleName}>"
}

typealias KoflFunction = (Map<String?, KoflObject>, MutableEnvironment) -> KoflObject

abstract class KoflObject internal constructor() {
  abstract override fun toString(): String
}

data class KoflAny(val any: Any) : KoflObject() {
  override fun toString() = any.toString()

  companion object : KoflType
}

object KoflUnit : KoflSingleton() {
  override fun toString(): String = "Unit"
}

data class KoflString(val string: String) : KoflObject(), CharSequence by string {
  operator fun plus(another: KoflObject) = copy(string = string + another)

  override fun toString() = string

  companion object : KoflPrimitive<String>(String::class, KoflString, KoflString) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject =
      KoflString(arguments.entries.first().value.toString())
  }
}

sealed class KoflNumber<T : Number> : KoflObject() {
  abstract val number: T

  abstract fun toDouble(): KoflDouble
  abstract fun toInt(): KoflInt

  abstract operator fun unaryPlus(): KoflNumber<T>
  abstract operator fun unaryMinus(): KoflNumber<T>
  abstract operator fun plus(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun minus(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun times(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun div(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun compareTo(another: KoflNumber<T>): Int

  override fun toString(): String = number.toString()
}

data class KoflDouble(override val number: Double) : KoflNumber<Double>() {
  override fun toDouble() = this
  override fun toInt() = KoflInt(number.toInt())

  override fun unaryPlus() = copy()
  override fun unaryMinus() = copy(number = -number)
  override fun plus(another: KoflNumber<Double>) = copy(number = number + another.number)
  override fun minus(another: KoflNumber<Double>) = copy(number = number - another.number)
  override fun times(another: KoflNumber<Double>) = copy(number = number * another.number)
  override fun div(another: KoflNumber<Double>) = copy(number = number / another.number)

  override fun compareTo(another: KoflNumber<Double>): Int = number.compareTo(another.number)

  override fun toString() = number.toString()

  companion object : KoflPrimitive<Double>(Double::class, KoflString, KoflDouble) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject =
      KoflDouble(arguments.entries.first().value.toString().toDouble())
  }
}

data class KoflInt(override val number: Int) : KoflNumber<Int>() {
  override fun toDouble() = KoflDouble(number.toDouble())
  override fun toInt() = this

  override fun unaryPlus() = copy()
  override fun unaryMinus() = copy(number = -number)
  override fun plus(another: KoflNumber<Int>) = copy(number = number + another.number)
  override fun minus(another: KoflNumber<Int>) = copy(number = number - another.number)
  override fun times(another: KoflNumber<Int>) = copy(number = number * another.number)
  override fun div(another: KoflNumber<Int>) = copy(number = number / another.number)

  override fun compareTo(another: KoflNumber<Int>): Int = number.compareTo(another.number)

  override fun toString() = number.toString()

  companion object : KoflPrimitive<Int>(Int::class, KoflString, KoflInt) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject =
      KoflInt(arguments.entries.first().value.toString().toInt())
  }
}

sealed class KoflBoolean(private val primitive: Boolean) : KoflObject() {
  operator fun not() = (!primitive).asKoflBoolean()

  object True : KoflBoolean(true) {
    override fun toString(): String = "true"
  }

  object False : KoflBoolean(false) {
    override fun toString(): String = "false"
  }

  companion object : KoflPrimitive<Boolean>(Boolean::class, KoflString, KoflBoolean) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject =
      if (arguments.entries.first().value.toString().toBoolean())
        True else False
  }
}

data class KoflInstance(val type: KoflStruct, val fields: Map<String, KoflValue>) : KoflObject() {
  override fun toString(): String = fields.entries
    .joinToString(prefix = "${type.name}(", postfix = ")")

  companion object : KoflType {
    override fun toString(): String = "<instance>"
  }
}

abstract class KoflCallable(
  val parameters: Map<String, KoflType>,
  val returnType: KoflType
) : KoflObject(), KoflType {
  abstract fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject
  abstract override fun toString(): String
}

@Suppress("UNCHECKED_CAST")
fun KoflObject.asKoflNumber(): KoflNumber<Number> = when (this) {
  is KoflAny -> when (any) {
    is Double -> KoflDouble(any) as KoflNumber<Number>
    is Int -> KoflInt(any) as KoflNumber<Number>
    else -> throw InvalidTypeException("int or double")
  }
  is KoflNumber<*> -> this as KoflNumber<Number>
  else -> throw InvalidDeclaredTypeException(type.toString(), "int or double")
}

fun Boolean.asKoflBoolean(): KoflBoolean = when (this) {
  true -> KoflBoolean.True
  false -> KoflBoolean.False
}

fun KoflObject.isTruthy(): Boolean = when (this) {
  is KoflBoolean.True -> true
  else -> false
}

fun Any.asKoflObject(): KoflObject = when (this) {
  Unit -> KoflUnit
  true -> KoflBoolean.True
  false -> KoflBoolean.False
  is KoflObject -> this
  is Double -> KoflDouble(this)
  is Int -> KoflInt(this)
  is String -> KoflString(this)
  else -> KoflAny(this)
}

operator fun KoflValue.component0(): KoflObject = value

fun Any.asKoflValue(mutable: Boolean = false): KoflValue {
  return asKoflObject().asKoflValue(mutable)
}

val KoflObject.type: KoflType
  get() = when (this) {
    is KoflString -> KoflString
    is KoflBoolean -> KoflBoolean
    is KoflInt -> KoflInt
    is KoflDouble -> KoflDouble
    is KoflCallable -> this
    else -> throw InvalidTypeException(this::class.toString())
  }

fun KoflObject.asKoflValue(mutable: Boolean): KoflValue {
  if (mutable) return KoflValue.Mutable(this, type)

  return KoflValue.Immutable(this, type)
}
