package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

sealed class KoflObject {
  protected abstract val value: Any

  class Wrapper(override val value: Any) : KoflObject()

  sealed class Callable : KoflObject() {
    override val value get() = ::call

    @PublishedApi
    internal abstract val descriptor: Descriptor

    @PublishedApi
    internal abstract fun call(arguments: Map<String, KoflObject>, environment: Environment)

    inline operator fun invoke(arguments: Map<String, KoflObject>, environment: Environment): KoflObject {
      try {
        call(arguments, environment)
      } catch (exception: ReturnException) {
        return exception.value
      }

      throw KoflRuntimeException.MissingReturn(descriptor)
    }

    class Function(private val evaluator: Evaluator, override val descriptor: FunctionDescriptor) : Callable() {
      override fun call(arguments: Map<String, KoflObject>, environment: Environment) {
        evaluator.evaluate(descriptor.body, environment.child {
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

    class NativeFunction(override val descriptor: NativeFunctionDescriptor) : Callable() {
      override fun call(arguments: Map<String, KoflObject>, environment: Environment) {
        TODO("make native call")
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
      override fun call(arguments: Map<String, KoflObject>, environment: Environment) {
        evaluator.evaluate(descriptor.body, environment.child {
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

  fun unwrap(): Any = value
}