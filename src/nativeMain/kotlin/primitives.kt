typealias KoflFunction = (List<KoflObject>, MutableEnvironment) -> KoflObject

abstract class KoflObject internal constructor() {
  abstract override fun toString(): String
}

object KoflUnit : KoflObject() {
  override fun toString(): String = "Unit"
}

data class KoflAny(val any: Any) : KoflObject() {
  override fun toString() = any.toString()
}

data class KoflString(val string: String) : KoflObject(), CharSequence by string {
  operator fun plus(another: KoflObject) = copy(string = string + another)

  override fun toString() = string
}

sealed class KoflNumber<T : Number> : KoflObject() {
  abstract val number: T

  abstract fun toDouble(): KoflDouble
  abstract fun toInt(): KoflInt

  abstract operator fun unaryPlus(): KoflNumber<T>
  abstract operator fun unaryMinus(): KoflNumber<T>
  abstract operator fun plus(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun minus(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun times(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun div(another: KoflNumber<T>): KoflNumber<T>
  abstract operator fun compareTo(another: KoflNumber<T>): Int

  override fun toString(): String = number.toString()
}

data class KoflDouble(override val number: Double) : KoflNumber<Double>() {
  override fun toDouble() = this
  override fun toInt() = KoflInt(number.toInt())

  override fun unaryPlus() = copy()
  override fun unaryMinus() = copy(number = -number)
  override fun plus(another: KoflNumber<Double>) = copy(number = number + another.number)
  override fun minus(another: KoflNumber<Double>) = copy(number = number - another.number)
  override fun times(another: KoflNumber<Double>) = copy(number = number * another.number)
  override fun div(another: KoflNumber<Double>) = copy(number = number / another.number)

  override fun compareTo(another: KoflNumber<Double>): Int = number.compareTo(another.number)

  override fun toString() = number.toString()
}

data class KoflInt(override val number: Int) : KoflNumber<Int>() {
  override fun toDouble() = KoflDouble(number.toDouble())
  override fun toInt() = this

  override fun unaryPlus() = copy()
  override fun unaryMinus() = copy(number = -number)
  override fun plus(another: KoflNumber<Int>) = copy(number = number + another.number)
  override fun minus(another: KoflNumber<Int>) = copy(number = number - another.number)
  override fun times(another: KoflNumber<Int>) = copy(number = number * another.number)
  override fun div(another: KoflNumber<Int>) = copy(number = number / another.number)

  override fun compareTo(another: KoflNumber<Int>): Int = number.compareTo(another.number)

  override fun toString() = number.toString()
}

sealed class KoflBoolean(private val primitive: Boolean) : KoflObject() {
  operator fun not() = (!primitive).asKoflBoolean()

  object True : KoflBoolean(true) {
    override fun toString(): String = "true"
  }

  object False : KoflBoolean(false) {
    override fun toString(): String = "false"
  }
}


data class KoflInstance(
  val type: KoflStruct,
  val fields: Map<String, KoflValue>
) : KoflObject() {
  operator fun get(name: Token): KoflValue? = fields[name.lexeme]
  operator fun set(name: Token, newValue: KoflObject): Unit =
    when (val value = this[name] ?: throw UnresolvedFieldError(name.lexeme, type)) {
      is KoflValue.Immutable -> throw IllegalOperationError(name, "update an immutable field")
      is KoflValue.Mutable -> value.value = newValue
    }

  override fun toString(): String = fields.entries
    .joinToString(
      prefix = "${type.name}(",
      postfix = ")"
    )
}

@Suppress("UNCHECKED_CAST")
fun KoflObject.asKoflNumber(): KoflNumber<Number> = when (this) {
  is KoflAny -> when (any) {
    is Double -> KoflDouble(any) as KoflNumber<Number>
    is Int -> KoflInt(any) as KoflNumber<Number>
    else -> throw TypeError("int or double")
  }
  is KoflNumber<*> -> this as KoflNumber<Number>
  else -> throw TypeError("int or double")
}

fun Boolean.asKoflBoolean(): KoflBoolean = when (this) {
  true -> KoflBoolean.True
  false -> KoflBoolean.False
}

fun KoflObject.isTruthy(): Boolean = when (this) {
  is KoflBoolean.True -> true
  else -> false
}

fun Any.asKoflObject(): KoflObject = when (this) {
  Unit -> KoflUnit
  true -> KoflBoolean.True
  false -> KoflBoolean.False
  is KoflObject -> this
  is Double -> KoflDouble(this)
  is Int -> KoflInt(this)
  else -> KoflAny(this)
}
