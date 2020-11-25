package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Expr

abstract class KoflCallable internal constructor(val parameters: List<KoflType>) : KoflObject() {
  operator fun invoke(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    return call(arguments, environment)
  }
  abstract fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject
  abstract override fun toString(): String

  class Native(parameters: List<KoflType>, private val nativeCall: KoflFunction) : KoflCallable(parameters) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      return nativeCall(arguments, environment)
    }

    override fun toString(): String = "<native func>"
  }

  class AnonymousFunc(
    parameters: List<KoflType>,
    private val decl: Expr.AnonymousFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEach { (name, value) ->
        if (name == null) return@forEach

        localEnvironment.define(name, value.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func <anonymous>(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }

      append(")")
    }
  }

  class Func(
    parameters: List<KoflType>,
    private val decl: Expr.CommonFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEach { (name, value) ->
        if (name == null) return@forEach

        localEnvironment.define(name, value.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func ${decl.name}(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }
      append(")")
    }
  }

  class ExtensionFunc(
    parameters: List<KoflType>,
    val receiver: KoflType,
    private val decl: Expr.ExtensionFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters) {
    private var self: KoflInstance? = null

    fun bind(self: KoflInstance) {
      this.self = self
    }

    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      // TODO: add a specific exception for that
      localEnvironment.define("this", self?.asKoflValue() ?: throw UnresolvedVarError("this"))

      arguments.forEach { (name, value) ->
        if (name == null) return@forEach

        localEnvironment.define(name, value.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func ${decl.receiver} ${decl.name}(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }
      append(")")
    }
  }

  data class Type(
    val parameters: Map<String, KoflType>,
    val returnType: KoflType,
  ) : KoflType {
    val signature = signature {
      parameters(parameters.values.toList())
    }

    override fun toString(): String = "func ${
      parameters.entries.joinToString(
        prefix = "(", postfix = ")"
      ) { (name, type) ->
        "$name: $type"
      }
    } -> $returnType"
  }
}
