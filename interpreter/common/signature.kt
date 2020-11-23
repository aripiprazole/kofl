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
  data class Name(val name: String) : Signature()
  data class Parameters(val types: List<KoflType>) : Signature()
  data class Receiver(val types: KoflType) : Signature()
  data class Combined(val name: Name, val parameters: Parameters, val receiver: Receiver? = null) : Signature()
}

class SignatureBuilder {
  private lateinit var name: Signature.Name
  private lateinit var parameters: Signature.Parameters
  private var receiver: Signature.Receiver? = null

  fun name(name: String) {
    this.name = Signature.Name(name)
  }

  fun parameters(vararg parameters: KoflType) {
    this.parameters = Signature.Parameters(parameters.toList())
  }

  fun receiver(receiver: KoflType) {
    this.receiver = Signature.Receiver(receiver)
  }

  fun build(): Signature = Signature.Combined(name, parameters, receiver)
}

fun signature(builder: SignatureBuilder.() -> Unit): Signature {
  return SignatureBuilder().apply(builder).build()
}