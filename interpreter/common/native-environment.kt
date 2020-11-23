package com.lorenzoog.kofl.interpreter

class NativeEnvironment : Environment {
  @OptIn(KoflResolverInternals::class)
  override fun get(name: Token): KoflValue = when (name.lexeme) {
    "Double" -> KoflDouble
    "Int" -> KoflInt
    "Unit" -> KoflUnit
    "Boolean" -> KoflBoolean

    "println" -> KoflCallable.Native(1) { (message), _ ->
      throw Return(println(message).asKoflObject())
    }

    "print" -> KoflCallable.Native(1) { (message), _ ->
      throw Return(print(message).asKoflObject())
    }

    else -> throw UnresolvedVarError(name)
  }.asKoflValue()

  override fun toString(): String = "<native env>"
}