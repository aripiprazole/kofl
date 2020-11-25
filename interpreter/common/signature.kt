package com.lorenzoog.kofl.interpreter

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
  data class Name internal constructor(val name: String) : Signature()
  data class Parameters internal constructor(val types: List<KoflType>) : Signature()
  data class Receiver internal constructor(val types: KoflType) : Signature()
  data class Combined internal constructor(
    val name: Name,
    val parameters: Parameters,
    val receiver: Receiver? = null
  ) : Signature()
}

class SignatureBuilder {
  private var name: Signature.Name? = null
  private var parameters: Signature.Parameters? = null
  private var receiver: Signature.Receiver? = null

  fun name(name: String) {
    this.name = Signature.Name(name)
  }

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
    name = name ?: error("MISSING NAME"),
    parameters = parameters ?: error("MISSING PARAMETERs"),
    receiver
  )
}

class FunctionOverload(private val name: String) : KoflCallable(31209381), KoflType {
  fun callByPattern(arguments: Map<String, KoflType>) {

  }

  override fun invoke(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    TODO("Not yet implemented")
  }

  override fun toString(): String = "<overload $name>"
}

class SignatureEnvironment(private val environment: SignatureEnvironment? = null) {
  private val types = mutableMapOf<String, KoflType>()
  private val variables = mutableMapOf<String, KoflType>()
  private val functions = mutableMapOf<Signature, KoflCallable.Type>()

  fun at(distance: Int): SignatureEnvironment {
    TODO()
  }

  fun defineFunction(signature: Signature, type: KoflCallable.Type) {
    signature as Signature.Combined

    functions[signature.name] = type
  }

  fun findFunction(signature: Signature): KoflCallable.Type {
    signature as Signature.Combined

    return functions[signature.name]!!
  }

  fun findName(name: String): KoflType {
    TODO()
  }

  fun findType(name: String): KoflType? {
    return types[name]
  }

  fun defineType(name: String, type: KoflType) {
    types[name] = type
  }
}

fun globalEnvironment(size: Int, builder: SignatureEnvironment.() -> Unit): Stack<SignatureEnvironment> {
  return Stack<SignatureEnvironment>(size).apply {
    push(SignatureEnvironment().apply(builder))
  }
}

fun signature(builder: SignatureBuilder.() -> Unit): Signature {
  return SignatureBuilder().apply(builder).build()
}
