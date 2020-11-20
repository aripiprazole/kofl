class NativeEnvironment : Environment {
  override fun get(name: Token): KoflValue = when (name.lexeme) {
    "println" -> KoflCallable.Native(1) { (message), _ ->
      println(message).asKoflObject()
    }.asKoflValue()

    "print" -> KoflCallable.Native(1) { (message), _ ->
      print(message).asKoflObject()
    }.asKoflValue()

    else -> throw UnsolvedReferenceError(name)
  }

  override fun toString(): String = "<native env>"
}