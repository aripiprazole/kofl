package com.lorenzoog.kofl.frontend

/**
 * Signature is made for be possible call-by-pattern,
 * and function overload in the environment, and can be useful
 * to isolate functions and variables.
 *
 * You can create a signature using the helper function [signature],
 * and use in [Environment.findFunction] to find functions that match
 * to signature provided, you can use the builder also to create native
 * functions with some type safety, the [KoflCallable.Native] will check
 * if match the signatures.
 */
sealed class Signature {
  data class Parameters internal constructor(val types: List<KoflType>) : Signature()
  data class Receiver internal constructor(val types: KoflType) : Signature()
  data class Combined internal constructor(
    val parameters: Parameters,
    val receiver: Receiver? = null
  ) : Signature()
}

class SignatureBuilder {
  private var parameters: Signature.Parameters? = null
  private var receiver: Signature.Receiver? = null

  fun parameters(vararg parameters: KoflType) {
    this.parameters = Signature.Parameters(parameters.toList())
  }

  fun parameters(parameters: List<KoflType>) {
    parameters(*parameters.toTypedArray())
  }

  fun parameters(parameters: Map<String, KoflType>) {
    parameters(*parameters.values.toTypedArray())
  }

  fun parameters(vararg parameters: Pair<String, KoflType>) {
    parameters(mapOf(*parameters))
  }

  fun receiver(receiver: KoflType) {
    this.receiver = Signature.Receiver(receiver)
  }

  fun build(): Signature = Signature.Combined(
    parameters = parameters ?: error("MISSING PARAMETERs"),
    receiver
  )
}

class TypeEnvironment(private val enclosing: TypeEnvironment? = null) {
  private val types = mutableMapOf<String, KoflType>()
  private val variables = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<String, KoflCallable.Type>()

  fun defineFunction(name: String, type: KoflCallable.Type) {
    functions[name] = type
  }

  fun findFunction(name: String): KoflCallable.Type {
    return functions[name]!!
  }

  fun findName(name: String): KoflType {
    TODO()
  }

  fun findTypeOrNull(name: String): KoflType? {
    return types[name] ?: enclosing?.findTypeOrNull(name)
  }

  fun findType(name: String): KoflType {
    return types[name] ?: enclosing?.findType(name) ?: throw UnresolvedVarException(name)
  }

  fun defineType(name: String, type: KoflType) {
    types[name] = type
  }
}

fun globalEnvironment(size: Int, builder: TypeEnvironment.() -> Unit): Stack<TypeEnvironment> {
  return Stack<TypeEnvironment>(size).apply {
    push(TypeEnvironment().apply(builder))
  }
}

fun signature(builder: SignatureBuilder.() -> Unit): Signature {
  return SignatureBuilder().apply(builder).build()
}
