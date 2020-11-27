package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

class NativeEnvironment : Environment {
  @OptIn(KoflResolverInternals::class)
  override fun get(name: Token): KoflValue = when (name.lexeme) {
    "Double" -> KoflDouble
    "Int" -> KoflInt
    "Unit" -> KoflUnit
    "Boolean" -> KoflBoolean

    "println" -> NativeFunc("println", mapOf("message" to KoflString), KoflUnit) { arguments, _ ->
      throw Return(println(arguments.entries.first().value).asKoflObject())
    }

    "print" -> NativeFunc("print", mapOf("message" to KoflString), KoflUnit) { arguments, _ ->
      throw Return(print(arguments.entries.first().value).asKoflObject())
    }

    else -> throw UnresolvedVarException(name)
  }.asKoflValue()

  override fun toString(): String = "<native env>"
}
