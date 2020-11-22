package com.lorenzoog.kofl.interpreter

abstract class KoflCallable internal constructor(val arity: Int) : KoflObject() {
  abstract operator fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject
  abstract override fun toString(): String

  class Native(arity: Int, private val call: KoflFunction) : KoflCallable(arity) {
    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      return call(arguments, environment)
    }

    override fun toString(): String = "<native func>"
  }

  class AnonymousFunc(private val decl: Expr.AnonymousFunc, private val evaluator: CodeEvaluator) :
    KoflCallable(decl.arguments.size) {

    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
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

  class Func(private val decl: Expr.Func, private val evaluator: CodeEvaluator) :
    KoflCallable(decl.arguments.size) {
    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
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

  class ExtensionFunc(private val decl: Expr.ExtensionFunc, private val evaluator: CodeEvaluator) :
    KoflCallable(decl.arguments.size) {
    private var self: KoflInstance? = null

    fun bind(self: KoflInstance) {
      this.self = self
    }

    override operator fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      // TODO: add a specific exception for that
      localEnvironment.define("this", self?.asKoflValue() ?: throw UnresolvedVarError("this"))

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
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
}
