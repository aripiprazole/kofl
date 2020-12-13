package com.lorenzoog.kofl.compiler.common.typing

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
  ) : Callable() {

    override fun hashCode(): Int {
      return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || other !is Function) return false

      if (parameters != other.parameters) return false
      if (returnType != other.returnType) return false
      if (receiver != other.receiver) return false

      return true
    }
  }

  data class Class(
    val name: String?,
    val constructors: List<Function>,
    override val fields: Map<String, KoflType>,
    override val functions: MutableMap<String, List<Function>>
  ) : Callable() {
    override val parameters: Map<String, KoflType> get() = fields
    override val returnType: KoflType = this

    override fun toString(): String = "$name"

    override fun hashCode(): Int {
      return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other == null || other !is Class) return false

      if (name != other.name) return false
      if (constructors != other.constructors) return false
      if (fields != other.fields) return false
      if (functions != other.functions) return false
      if (returnType != other.returnType) return false

      return true
    }
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

