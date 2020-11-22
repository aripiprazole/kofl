package com.lorenzoog.kofl.interpreter

import kotlin.reflect.KClass

interface KoflType

abstract class KoflSingleton<T : Any>(kClass: KClass<T>) : KoflType {
  abstract override fun toString(): String
}

abstract class KoflPrimitive<T : Any>(private val kClass: KClass<T>, arity: Int = 1) : KoflCallable(arity), KoflType {
  override fun toString(): String = "<primitive ${kClass.simpleName}>"
}

class KoflStruct(stmt: Stmt.TypeDef.Struct) : KoflCallable(stmt.fieldsDef.size) {
  private val fieldsDef = stmt.fieldsDef
  val functions = mutableMapOf<String, ExtensionFunc>()
  val name = stmt.name

  override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
    val fields = mutableMapOf<String, KoflValue>()

    arguments.forEachIndexed { i, argument ->
      fields[fieldsDef[i].lexeme] = argument.asKoflValue()
    }

    val instance = KoflInstance(this, fields)

    functions.forEach { (_, func) ->
      func.bind(instance)
    }

    return instance
  }

  override fun toString(): String = "struct $name"
}

