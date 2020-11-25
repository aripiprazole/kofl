package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Expr

abstract class KoflCallable internal constructor(
  val parameters: Map<String, KoflType>,
  val returnType: KoflType
) : KoflObject(), KoflType {
  operator fun invoke(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    return call(arguments, environment)
  }

  abstract fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject
  abstract override fun toString(): String

  class Native(
    parameters: Map<String, KoflType>,
    returnType: KoflType,
    private val nativeCall: KoflFunction
  ) : KoflCallable(parameters, returnType) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      return nativeCall(arguments, environment)
    }

    override fun toString(): String = "func <native func>(): $returnType"
  }

  class AnonymousFunc(
    parameters: Map<String, KoflType>,
    returnType: KoflType,
    private val decl: Expr.AnonymousFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters, returnType) {
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

      append("): $returnType")
    }
  }

  class Func(
    parameters: Map<String, KoflType>,
    returnType: KoflType,
    private val decl: Expr.CommonFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters, returnType) {
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
      append("): $returnType")
    }
  }

  class ExtensionFunc(
    parameters: Map<String, KoflType>,
    returnType: KoflType,
    val receiver: KoflType,
    private val decl: Expr.ExtensionFunc,
    private val evaluator: CodeEvaluator
  ) : KoflCallable(parameters, returnType) {
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
      append("func ${receiver} ${decl.name}(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }
      append("): $returnType")
    }
  }

  data class Type(val parameters: Map<String, KoflType>, val returnType: KoflType) : KoflType
}
