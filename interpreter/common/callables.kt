package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*

class InvalidParameterNameException(name: String, func: KoflCallable) :
  KoflRuntimeException("trying to call $func with a parameter $name that not exists")

class InvalidParameterTypeException(name: String, current: KoflType, expected: KoflType?, func: KoflCallable) :
  KoflRuntimeException("trying to call $func with a parameter $name with type $current and expected $expected")

class InvalidCallArityException(gotArity: Int, func: KoflCallable) :
  KoflRuntimeException("trying to call a $func with arity: ${func.parameters.size} and got $gotArity")

operator fun KoflCallable.invoke(arguments: Map<String?, KoflObject>, environment: MutableEnvironment): KoflObject {
  if(arguments.size != parameters.size) throw InvalidCallArityException(arguments.size, this)

  arguments.entries.forEachIndexed { index, (name, value) ->
    if (name != null) {
      if (name !in parameters.keys) throw InvalidParameterNameException(name, this)

      val paramType = parameters[name]
      if (value.evaluatedType != paramType) throw InvalidParameterTypeException(name, value.evaluatedType, paramType, this)

      return@forEachIndexed
    }

    val (paramName, paramType) = parameters.entries.toList()[index]
    if (value.evaluatedType != paramType) throw InvalidParameterTypeException(paramName, value.evaluatedType, paramType, this)
  }

  return call(arguments, environment)
}

class NativeFunc(
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

    localEnvironment.define("this", self?.asKoflValue() ?: throw UnresolvedVarException("this"))

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

val Any.koflType: KoflType
  get() = when (this) {
    is String -> KoflString
    is Double -> KoflDouble
    is Int -> KoflInt
    is Boolean -> KoflBoolean
    else -> throw TypeException(this::class.toString())
  }

val KoflObject.evaluatedType: KoflType
  get() = when (this) {
    is KoflString -> KoflString
    is KoflBoolean -> KoflBoolean
    is KoflInt -> KoflInt
    is KoflDouble -> KoflDouble
    is KoflCallable -> this
    else -> throw TypeException(this::class.toString())
  }
