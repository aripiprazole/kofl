class NativeEnvironment : Environment {
  override fun get(name: Token): Value = when (name.lexeme) {
    "println" -> KoflCallable.Native(1) { (message), _ ->
      println(message)
    }.asValue()

    "print" -> KoflCallable.Native(1) { (message), _ ->
      print(message)
    }.asValue()

    else -> throw UnsolvedReferenceError(name)
  }
}