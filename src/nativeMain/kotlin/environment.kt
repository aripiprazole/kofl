sealed class KoflValue {
  abstract val value: KoflObject

  data class Immutable(override val value: KoflObject) : KoflValue()
  data class Mutable(override var value: KoflObject) : KoflValue()
}

interface Environment {
  operator fun get(name: Token): KoflValue
}

interface MutableEnvironment : Environment {
  fun define(name: Token, value: KoflValue)
  operator fun set(name: Token, newValue: KoflObject)
}

fun MutableEnvironment(enclosing: Environment? = null): MutableEnvironment {
  return KoflEnvironment(enclosing)
}

private class KoflEnvironment(private val enclosing: Environment? = null) : MutableEnvironment {
  private val values = mutableMapOf<String, KoflValue>()

  override fun define(name: Token, value: KoflValue) = if (values[name.lexeme] == null) {
    values[name.lexeme] = value
  } else throw IllegalOperationError(name, "define a variable that already exists")

  override operator fun set(name: Token, newValue: KoflObject) = when (val value = this[name]) {
    is KoflValue.Immutable -> throw IllegalOperationError(name, "update an immutable variable")
    is KoflValue.Mutable -> value.value = newValue
  }

  override operator fun get(name: Token): KoflValue = values[name.lexeme]
    ?: enclosing?.get(name)
    ?: throw UnsolvedReferenceError(name)
}

fun Any.asKoflValue(mutable: Boolean = false): KoflValue {
  return asKoflObject().asKoflValue(mutable)
}

fun KoflObject.asKoflValue(mutable: Boolean): KoflValue {
  if (mutable) return KoflValue.Mutable(this)

  return KoflValue.Immutable(this)
}
