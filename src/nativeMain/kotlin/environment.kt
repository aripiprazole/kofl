sealed class Value {
  abstract val value: Any

  data class Immutable(override val value: Any) : Value()
  data class Mutable(override var value: Any) : Value()
}

fun Any.asValue(): Value.Immutable {
  return Value.Immutable(this)
}

interface Environment {
  operator fun get(name: Token): Value
}

interface MutableEnvironment : Environment {
  fun define(name: Token, value: Any, immutable: Boolean = true)
  operator fun set(name: Token, newValue: Any)
}

fun MutableEnvironment(enclosing: Environment? = null): MutableEnvironment {
  return KoflEnvironment(enclosing)
}

private class KoflEnvironment(private val enclosing: Environment? = null) : MutableEnvironment {
  private val values = mutableMapOf<String, Value>()

  override fun define(name: Token, value: Any, immutable: Boolean) = if (values[name.lexeme] == null) {
    values[name.lexeme] =
      if (immutable) Value.Immutable(value)
      else Value.Mutable(value)
  } else throw IllegalOperationError(name, "define a variable that already exists")

  override operator fun set(name: Token, newValue: Any) = when (val value = this[name]) {
    is Value.Immutable -> throw IllegalOperationError(name, "update an immutable variable")
    is Value.Mutable -> value.value = newValue
  }

  override operator fun get(name: Token): Value = values[name.lexeme]
    ?: enclosing?.get(name)
    ?: throw UnsolvedReferenceError(name)
}