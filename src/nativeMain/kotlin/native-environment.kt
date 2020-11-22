class NativeEnvironment : Environment {
  private val environment = mapOf(
    "Double" to KoflDouble,
    "Int" to KoflInt,
    "Unit" to KoflUnit,
    "Boolean" to KoflBoolean,

    "println" to KoflCallable.Native(1) { (message), _ ->
      println(message).asKoflObject()
    },

    "print" to KoflCallable.Native(1) { (message), _ ->
      print(message).asKoflObject()
    }
  )

  @OptIn(KoflResolverInternals::class)
  override fun get(name: Token): KoflValue =
    environment[name.lexeme]?.asKoflValue() ?: throw UnresolvedVarError(name)

  override fun toString(): String = "<native env>"
}