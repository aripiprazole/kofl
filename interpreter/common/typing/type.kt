package com.lorenzoog.kofl.interpreter.typing

sealed class KoflType {
  open val fields: Map<String, KoflType> = mapOf()
  open val functions: MutableMap<String, List<Function>> = mutableMapOf()

  operator fun get(name: String): KoflType? = fields[name]

  abstract class Callable internal constructor() : KoflType() {
    abstract val parameters: Map<String, KoflType>
    abstract val returnType: KoflType
  }

  data class Function(
    override val parameters: Map<String, KoflType>,
    override val returnType: KoflType,
    val receiver: KoflType? = null
  ) : Callable()

  data class Class(
    override val fields: Map<String, KoflType>,
    override val functions: MutableMap<String, List<Function>>
  ) : Callable() {
    override val parameters: Map<String, KoflType> get() = fields
    override val returnType: KoflType = this
  }

  object Primitive {
    val Any = createClass("Any")
    val String = createClass("String")
    val Unit = createClass("Unit")
    val Double = createClass("Double")
    val Int = createClass("Int")
    val Boolean = createClass("Boolean")
  }
}

