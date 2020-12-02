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
    val name: String?,
    val constructors: List<Function>,
    override val fields: Map<String, KoflType>,
    override val functions: MutableMap<String, List<Function>>
  ) : Callable() {
    override val parameters: Map<String, KoflType> get() = fields
    override val returnType: KoflType = this

    override fun toString(): String = "$name(fields=$fields, functions=$functions)"
  }

  companion object Primitive {
    val Any = createClassDefinition("Any")
    val String = createClassDefinition("String") {
      constructor("any" to Any)
    }
    val Unit = createClassDefinition("Unit")
    val Double = createClassDefinition("Double")
    val Int = createClassDefinition("Int")
    val Boolean = createClassDefinition("Boolean")
  }
}

