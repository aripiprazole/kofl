package com.lorenzoog.kofl.interpreter

import kotlin.reflect.KClass

interface KoflType

abstract class KoflSingleton<T : Any>(kClass: KClass<T>) : KoflType {
  abstract override fun toString(): String
}

abstract class KoflPrimitive<T : Any>(
  private val kClass: KClass<T>,
  parameterType: KoflType,
  returnType: KoflType,
) : KoflCallable(mapOf("raw" to parameterType), returnType), KoflType {
  override fun toString(): String = "<primitive ${kClass.simpleName}>"
}

class KoflStruct(
  val name: String,
  private val fields: Map<String, KoflType>
) : KoflCallable(fields, KoflInstance), KoflType {
  val functions = mutableMapOf<String, ExtensionFunc>()

  override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    val fieldsList = fields.entries.toList()
    val fields = mutableMapOf<String, KoflValue>()

    arguments.entries.forEachIndexed { i, argument ->
      fields[fieldsList[i].key] = argument.value.asKoflValue()
    }

    val instance = KoflInstance(this, fields)

    functions.forEach { (_, func) ->
      func.bind(instance)
    }

    return instance
  }

  override fun toString(): String = "struct $name"
}

val Any.koflType: KoflType
  get() = when (this) {
    is String -> KoflString
    is Double -> KoflDouble
    is Int -> KoflInt
    is Boolean -> KoflBoolean
    else -> throw TypeError(this::class.toString())
  }

val KoflObject.type: KoflType
  get() = when (this) {
    is KoflString -> KoflString
    is KoflBoolean -> KoflBoolean
    is KoflInt -> KoflInt
    is KoflDouble -> KoflDouble
    is KoflCallable -> this
    else -> throw TypeError(this::class.toString())
  }
