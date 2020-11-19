sealed class Value {
  abstract val value: Any

  data class Immutable(override val value: Any) : Value()
  data class Mutable(override var value: Any) : Value()
}

class Environment {
  private val values = mutableMapOf<String, Value>()

  fun define(name: Token, value: Any, immutable: Boolean = true) = if (values[name.lexeme] == null) {
    values[name.lexeme] =
      if (immutable)
        Value.Immutable(value)
      else
        Value.Mutable(value)
  } else throw IllegalOperationError(name, "define a variable that already exists")

  operator fun set(name: Token, newValue: Any) = when (val value = this[name]) {
    is Value.Immutable -> throw IllegalOperationError(name, "update an immutable variable")
    is Value.Mutable -> value.value = newValue
  }

  operator fun get(name: Token) = values[name.lexeme]
    ?: throw UnsolvedReferenceError(name)
}