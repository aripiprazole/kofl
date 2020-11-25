package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.Expr

class InvalidParameterNameError(name: String, func: KoflCallable) :
  KoflRuntimeError("trying to call $func with a parameter $name that not exists")

class InvalidParameterTypeError(name: String, current: KoflType, expected: KoflType?, func: KoflCallable) :
  KoflRuntimeError("trying to call $func with a parameter $name with type $current and expected $expected")

class InvalidCallArityError(gotArity: Int, func: KoflCallable) :
  KoflRuntimeError("trying to call a $func with arity: ${func.parameters.size} and got $gotArity")

abstract class KoflCallable internal constructor(
  val parameters: Map<String, KoflType>,
  val returnType: KoflType
) : KoflObject(), KoflType {
  operator fun invoke(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
    if(arguments.size != parameters.size) throw InvalidCallArityError(arguments.size, this)

    arguments.entries.forEachIndexed { index, (name, value) ->
      if (name != null) {
        if (name !in parameters.keys) throw InvalidParameterNameError(name, this)

        val paramType = parameters[name]
        if (value.type != paramType) throw InvalidParameterTypeError(name, value.type, paramType, this)

        return@forEachIndexed
      }

      val (paramName, paramType) = parameters.entries.toList()[index]
      if (value.type != paramType) throw InvalidParameterTypeError(paramName, value.type, paramType, this)
    }

    return call(arguments, environment)
  }

  abstract fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject
  abstract override fun toString(): String

  class Native(
    private val name: String,
    parameters: Map<String, KoflType>,
    returnType: KoflType,
    private val nativeCall: KoflFunction
  ) : KoflCallable(parameters, returnType) {
    override fun call(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
      return nativeCall(arguments, environment)
    }

    override fun toString(): String = "func $name(): $returnType"
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
      append("func $receiver ${decl.name}(")

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
