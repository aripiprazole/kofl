package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException
import com.lorenzoog.kofl.interpreter.runtime.KoflNativeCallable
import com.lorenzoog.kofl.interpreter.runtime.NativeEnvironment
import com.lorenzoog.kofl.interpreter.typing.KoflType

sealed class KoflObject {
  protected abstract val value: Any

  class Wrapper(override val value: Any) : KoflObject()

  data class Instance(
    val definition: Class,
    val fields: Map<String, Value>
  ) : KoflObject() {
    override val value: Instance get() = this
  }

  sealed class Class : KoflObject() {
    data class KoflClass(
      val definition: KoflType.Class,
      val constructors: List<Callable>,
      val functions: Map<String, List<Callable>>,
    ) : Class() {
      override val value: Any get() = definition
    }

    data class Singleton(
      public override val value: Instance,
      val definition: KoflType.Class,
      val constructors: List<Callable>,
      val functions: Map<String, List<Callable>>,
    ) : Class()
  }

  sealed class Callable : KoflObject() {
    override val value get() = ::call

    @PublishedApi
    internal abstract val descriptor: Descriptor

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
        // TODO return unit if hasn't returned nothing
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

  companion object {
    operator fun invoke(value: Any): KoflObject {
      return Wrapper(value)
    }
  }

  fun map(fmap: (Any) -> Any): KoflObject {
    return KoflObject(fmap(value))
  }

  fun flatMap(fmap: (Any) -> KoflObject): KoflObject {
    return fmap(value)
  }

  fun unwrap(): Any = value
}