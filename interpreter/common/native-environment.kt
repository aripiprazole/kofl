package com.lorenzoog.kofl.interpreter

class NativeEnvironment : Environment {
  @OptIn(KoflResolverInternals::class)
  override fun get(name: Token): KoflValue = when (name.lexeme) {
    "Double" -> KoflDouble
    "Int" -> KoflInt
    "Unit" -> KoflUnit
    "Boolean" -> KoflBoolean

    "println" -> KoflCallable.Native(1) { arguments, _ ->
      throw Return(println(arguments.entries.first().value).asKoflObject())
    }

    "print" -> KoflCallable.Native(1) { arguments, _ ->
      throw Return(print(arguments.entries.first().value).asKoflObject())
    }

    else -> throw UnresolvedVarError(name)
  }.asKoflValue()

  override fun toString(): String = "<native env>"
}