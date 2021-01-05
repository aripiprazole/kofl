@file:Suppress("RedundantUnitExpression")

package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.backend.CallableDescriptor
import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.backend.FunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.LocalFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.NativeFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

sealed class KoflObject(val fields: Map<String, Value> = mapOf()) {
  abstract val definition: KfType

  protected abstract val value: Any

  class NativeObject(override val value: Any, override val definition: KfType) : KoflObject()

  class Instance(override val definition: KfType) : KoflObject() {
    override val value: Instance get() = this
  }

  data class Class(
    override val definition: KfType.Class,
    val constructors: List<Callable>,
    val functions: Map<String, List<Callable>>,
    val inherits: Collection<KfType> = listOf()
  ) : KoflObject() {
    override val value: Any get() = definition
  }

  sealed class Callable : KoflObject() {
    override val value get() = ::call
    override val definition: KfType
      get() = KfType.Function(
        descriptor.parameters,
        descriptor.returnType
      )

    abstract val descriptor: CallableDescriptor

    protected abstract fun call(
      callSite: Descriptor,
      arguments: Map<String, KoflObject>,
      environment: Environment
    )

    operator fun invoke(
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

    class Function(private val evaluator: Evaluator, override val descriptor: FunctionDescriptor) :
      Callable() {
      override fun call(
        callSite: Descriptor,
        arguments: Map<String, KoflObject>,
        environment: Environment
      ) {
        evaluator.evaluate(
          descriptor.body,
          environment.child(callSite) {
            arguments.forEach { (name, value) -> declare(name, Value.Immutable(value)) }
          }
        )
      }

      override fun toString(): String = buildString {
        append(
          descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
            "$name: $type"
          }
        )
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class LocalNativeFunction(
      private val nativeCall: KoflNativeCallable,
      override val descriptor: NativeFunctionDescriptor,
    ) : Callable() {
      override fun call(
        callSite: Descriptor,
        arguments: Map<String, KoflObject>,
        environment: Environment
      ) {
        throw ReturnException(nativeCall(callSite, arguments, environment))
      }

      override fun toString(): String = buildString {
        append("@LocalNativeCall(\"${descriptor.nativeCall}\") ")
        append(
          descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
            "$name: $type"
          }
        )
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class NativeFunction(
      private val nativeEnvironment: NativeEnvironment,
      override val descriptor: NativeFunctionDescriptor
    ) : Callable() {
      override fun call(
        callSite: Descriptor,
        arguments: Map<String, KoflObject>,
        environment: Environment
      ) {
        nativeEnvironment.call(descriptor.nativeCall, callSite, arguments, environment)
      }

      override fun toString(): String = buildString {
        append("@NativeCall(\"${descriptor.nativeCall}\") ")
        append(
          descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
            "$name: $type"
          }
        )
        append(" -> ")
        append(descriptor.returnType)
      }
    }

    class LocalFunction(
      private val evaluator: Evaluator,
      override val descriptor: LocalFunctionDescriptor
    ) : Callable() {
      override fun call(
        callSite: Descriptor,
        arguments: Map<String, KoflObject>,
        environment: Environment
      ) {
        evaluator.evaluate(
          descriptor.body,
          environment.child(callSite) {
            arguments.forEach { (name, value) -> declare(name, Value.Immutable(value)) }
          }
        )
      }

      override fun toString(): String = buildString {
        append("@Local ")
        append(
          descriptor.parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
            "$name: $type"
          }
        )
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

  fun tmap(definition: KfType, fmap: (Any) -> Any): KoflObject {
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
    val Unit = KoflObject(kotlin.Unit, KfType.Unit)

    operator fun invoke(value: Any, type: KfType = KfType.Any): KoflObject {
      if (value is Unit) return Unit

      return NativeObject(value, type)
    }
  }
}
