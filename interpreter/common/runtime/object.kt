package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.interpreter.backend.*
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException
import com.lorenzoog.kofl.interpreter.typing.KoflType

sealed class KoflObject(val fields: Map<String, Value> = mapOf()) {
  abstract val definition: KoflType

  protected abstract val value: Any

  class NativeObject(override val value: Any, override val definition: KoflType) : KoflObject()

  class Instance(override val definition: KoflType) : KoflObject() {
    override val value: Instance get() = this
  }

  data class Class(
    override val definition: KoflType.Class,
    val constructors: List<Callable>,
    val functions: Map<String, List<Callable>>,
    val inherits: Collection<KoflType> = listOf()
  ) : KoflObject() {
    override val value: Any get() = definition
  }

  sealed class Callable : KoflObject() {
    override val value get() = ::call
    override val definition: KoflType get() = KoflType.Function(descriptor.parameters, descriptor.returnType)

    @PublishedApi
    internal abstract val descriptor: CallableDescriptor

    @PublishedApi
    internal abstract fun call(callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment)

    inline operator fun invoke(
      callSite: Descriptor,
      arguments: Map<String, KoflObject>,
      environment: Environment
    ): KoflObject {
      try {
        call(callSite, arguments, environment)
      } catch (exception: ReturnException) {
        return exception.value
      }

      throw KoflRuntimeException.MissingReturn(descriptor, environment)
    }

    class Function(private val evaluator: Evaluator, override val descriptor: FunctionDescriptor) : Callable() {
      override fun call(callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) {
        evaluator.evaluate(descriptor.body, environment.child(callSite) {
          arguments.forEach { (name, value) -> declare(name, Value.Immutable(value)) }
        })
      }

      override fun toString(): String = buildString {
        append(descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
          "$name: $type"
        })
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class LocalNativeFunction(
      private val nativeCall: KoflNativeCallable,
      override val descriptor: NativeFunctionDescriptor,
    ) : Callable() {
      override fun call(callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) {
        throw ReturnException(nativeCall(callSite, arguments, environment))
      }

      override fun toString(): String = buildString {
        append("@LocalNativeCall(\"${descriptor.nativeCall}\") ")
        append(descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
          "$name: $type"
        })
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class NativeFunction(
      private val nativeEnvironment: NativeEnvironment,
      override val descriptor: NativeFunctionDescriptor
    ) : Callable() {
      override fun call(callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) {
        nativeEnvironment.call(descriptor.nativeCall, callSite, arguments, environment)
      }

      override fun toString(): String = buildString {
        append("@NativeCall(\"${descriptor.nativeCall}\") ")
        append(descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
          "$name: $type"
        })
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class LocalFunction(
      private val evaluator: Evaluator,
      override val descriptor: LocalFunctionDescriptor
    ) : Callable() {
      override fun call(callSite: Descriptor, arguments: Map<String, KoflObject>, environment: Environment) {
        evaluator.evaluate(descriptor.body, environment.child(callSite) {
          arguments.forEach { (name, value) -> declare(name, Value.Immutable(value)) }
        })
      }

      override fun toString(): String = buildString {
        append("@Local ")
        append(descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
          "$name: $type"
        })
        append(" -> ")
        append(descriptor.returnType)
      }
    }
  }

  fun isTruthy(): Boolean {
    return value == true
  }

  fun map(fmap: (Any) -> Any): KoflObject {
    return KoflObject(fmap(value), definition)
  }

  fun tmap(definition: KoflType, fmap: (Any) -> Any): KoflObject {
    return KoflObject(fmap(value), definition)
  }

  fun flatMap(fmap: (Any) -> KoflObject): KoflObject {
    return fmap(value)
  }

  fun unwrap(): Any {
    return value
  }

  override fun toString(): String {
    return value.toString()
  }

  companion object {
    val Unit = KoflObject(kotlin.Unit, KoflType.Unit)

    operator fun invoke(value: Any, type: KoflType = KoflType.Any): KoflObject {
      return NativeObject(value, type)
    }
  }
}