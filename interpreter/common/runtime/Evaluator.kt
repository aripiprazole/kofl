package com.lorenzoog.kofl.interpreter.runtime

import com.lorenzoog.kofl.compiler.common.backend.AccessFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.AccessVarDescriptor
import com.lorenzoog.kofl.compiler.common.backend.AssignDescriptor
import com.lorenzoog.kofl.compiler.common.backend.BinaryDescriptor
import com.lorenzoog.kofl.compiler.common.backend.BlockDescriptor
import com.lorenzoog.kofl.compiler.common.backend.CallDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ClassDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ConstDescriptor
import com.lorenzoog.kofl.compiler.common.backend.Descriptor
import com.lorenzoog.kofl.compiler.common.backend.FunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.GetDescriptor
import com.lorenzoog.kofl.compiler.common.backend.IfDescriptor
import com.lorenzoog.kofl.compiler.common.backend.LocalFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.LogicalDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ModuleDescriptor
import com.lorenzoog.kofl.compiler.common.backend.NativeDescriptor
import com.lorenzoog.kofl.compiler.common.backend.NativeFunctionDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ReturnDescriptor
import com.lorenzoog.kofl.compiler.common.backend.SetDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ThisDescriptor
import com.lorenzoog.kofl.compiler.common.backend.UnaryDescriptor
import com.lorenzoog.kofl.compiler.common.backend.UseDescriptor
import com.lorenzoog.kofl.compiler.common.backend.ValDescriptor
import com.lorenzoog.kofl.compiler.common.backend.VarDescriptor
import com.lorenzoog.kofl.compiler.common.backend.WhileDescriptor
import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

class Evaluator(private val locals: MutableMap<Descriptor, Int>) {
  internal val globalEnvironment = Environment(isGlobal = true)

  private var isInitialized = false
  private val nativeEnvironment = NativeEnvironment()
  private val modules = mutableMapOf<String, Environment>()

  fun evaluate(
    descriptors: Collection<Descriptor>,
    environment: Environment = globalEnvironment
  ): Collection<KoflObject> {
    if (!isInitialized) {
      Builtin(globalEnvironment).setup()

      isInitialized = true
    }

    return descriptors.map {
      evaluate(it, environment)
    }
  }

  fun evaluate(descriptor: Descriptor, environment: Environment = globalEnvironment): KoflObject =
    when (descriptor) {
      is ConstDescriptor -> evaluateConstDescriptor(descriptor)
      is ThisDescriptor -> evaluateThisDescriptor(descriptor, environment)
      is SetDescriptor -> evaluateSetDescriptor(descriptor, environment)
      is GetDescriptor -> evaluateGetDescriptor(descriptor, environment)
      is CallDescriptor -> evaluateCallDescriptor(descriptor, environment)
      is AccessVarDescriptor -> evaluateAccessVarDescriptor(descriptor, environment)
      is AccessFunctionDescriptor -> evaluateAccessFunctionDescriptor(descriptor, environment)
      is UnaryDescriptor -> evaluateUnaryDescriptor(descriptor, environment)
      is ValDescriptor -> evaluateValDescriptor(descriptor, environment)
      is VarDescriptor -> evaluateVarDescriptor(descriptor, environment)
      is AssignDescriptor -> evaluateAssignDescriptor(descriptor, environment)
      is ReturnDescriptor -> evaluateReturnDescriptor(descriptor, environment)
      is BlockDescriptor -> evaluateBlockDescriptor(descriptor, environment)
      is WhileDescriptor -> evaluateWhileDescriptor(descriptor, environment)
      is IfDescriptor -> evaluateIfDescriptor(descriptor, environment)
      is LogicalDescriptor -> evaluateLogicalDescriptor(descriptor, environment)
      is BinaryDescriptor -> evaluateBinaryDescriptor(descriptor, environment)
      is LocalFunctionDescriptor -> evaluateLocalFunctionDescriptor(descriptor)
      is NativeFunctionDescriptor -> evaluateNativeFunctionDescriptor(descriptor, environment)
      is FunctionDescriptor -> evaluateFunctionDescriptor(descriptor, environment)
      is ClassDescriptor -> evaluateClassDescriptor(descriptor, environment)
      is NativeDescriptor -> evaluateNativeDescriptor()
      is UseDescriptor -> evaluateUseDescriptor(descriptor, environment)
      is ModuleDescriptor -> evaluateModuleDescriptor(descriptor, environment)
    }

  private fun evaluateConstDescriptor(descriptor: ConstDescriptor): KoflObject =
    when (val value = descriptor.value) {
      is Boolean -> KoflObject(value, KfType.Boolean)
      is String -> KoflObject(value, KfType.String)
      is Int -> KoflObject(value, KfType.Int)
      is Double -> KoflObject(value, KfType.Double)
      is Unit -> KoflObject.Unit
      is KoflObject.Instance -> value
      else -> KoflObject(value, descriptor.type)
    }

  private fun evaluateThisDescriptor(
    descriptor: ThisDescriptor,
    environment: Environment
  ): KoflObject {
    return lookup(descriptor, environment, "this")
  }

  private fun evaluateAccessVarDescriptor(
    descriptor: AccessVarDescriptor,
    environment: Environment
  ): KoflObject {
    return lookup(descriptor, environment, descriptor.name)
  }

  private fun evaluateAccessFunctionDescriptor(
    descriptor: AccessFunctionDescriptor,
    environment: Environment
  ): KoflObject {
    return lookup(descriptor, environment, descriptor.name)
  }

  private fun evaluateGetDescriptor(
    descriptor: GetDescriptor,
    environment: Environment
  ): KoflObject {
    TODO("get descriptor")
  }

  private fun evaluateSetDescriptor(
    descriptor: SetDescriptor,
    environment: Environment
  ): KoflObject {
    TODO("set descriptor")
  }

  private fun evaluateUnaryDescriptor(
    descriptor: UnaryDescriptor,
    environment: Environment
  ): KoflObject {
    val op = descriptor.op
    val right = evaluate(descriptor.right, environment)

    return when (op) {
      TokenType.Minus -> when (val value = right.unwrap()) {
        is Double -> KoflObject(-value, KfType.Double)
        is Int -> KoflObject(-value, KfType.Int)
        else -> right
      }
      TokenType.Bang -> KoflObject(!right.isTruthy(), KfType.Boolean)
      else -> right
    }
  }

  private fun evaluateCallDescriptor(
    descriptor: CallDescriptor,
    environment: Environment
  ): KoflObject {
    val callee = when (val callee = descriptor.callee) {
      is AccessFunctionDescriptor -> lookupFunction(callee, environment, callee.name)
      is AccessVarDescriptor -> lookup(callee, environment, callee.name)
      else -> evaluate(callee, environment)
    }
    val arguments = descriptor.arguments.mapValues { (_, value) ->
      evaluate(value, environment)
    }

    if (callee !is KoflObject.Callable)
      throw KoflRuntimeException.InvalidType(KoflObject.Callable::class, callee, environment)

    return callee(descriptor, arguments, environment)
  }

  private fun evaluateValDescriptor(
    descriptor: ValDescriptor,
    environment: Environment
  ): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      environment.declare(descriptor.name, Value.Immutable(data))
    }
  }

  private fun evaluateVarDescriptor(
    descriptor: VarDescriptor,
    environment: Environment
  ): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      environment.declare(descriptor.name, Value.Mutable(data))
    }
  }

  private fun evaluateAssignDescriptor(
    descriptor: AssignDescriptor,
    environment: Environment
  ): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      assign(descriptor, environment, descriptor.name, data)
    }
  }

  private fun evaluateReturnDescriptor(
    descriptor: ReturnDescriptor,
    environment: Environment
  ): KoflObject {
    throw ReturnException(evaluate(descriptor.value, environment))
  }

  private fun evaluateBlockDescriptor(
    descriptor: BlockDescriptor,
    environment: Environment
  ): KoflObject {
    val result = evaluate(descriptor.body, environment.child(descriptor))

    return result.lastOrNull() ?: KoflObject.Unit
  }

  private fun evaluateWhileDescriptor(
    descriptor: WhileDescriptor,
    environment: Environment
  ): KoflObject {
    while (evaluate(descriptor.condition, environment).isTruthy()) {
      evaluate(descriptor.body, environment.child(descriptor))
    }

    return KoflObject.Unit
  }

  private fun evaluateIfDescriptor(descriptor: IfDescriptor, environment: Environment): KoflObject {
    val condition = evaluate(descriptor.condition, environment)

    return if (condition.isTruthy()) {
      evaluate(descriptor.then, environment.child(descriptor)).lastOrNull()
    } else {
      evaluate(descriptor.orElse, environment.child(descriptor)).lastOrNull()
    } ?: KoflObject.Unit
  }

  private fun evaluateLogicalDescriptor(
    descriptor: LogicalDescriptor,
    environment: Environment
  ): KoflObject {
    val op = descriptor.op
    val left = evaluate(descriptor.left, environment).unwrap()
    val right = evaluate(descriptor.left, environment).unwrap()

    TODO("logical descriptor")
  }

  private fun evaluateBinaryDescriptor(
    descriptor: BinaryDescriptor,
    environment: Environment
  ): KoflObject {
    val op = descriptor.op
    val left = evaluate(descriptor.left).unwrap()
    val right = evaluate(descriptor.right).unwrap()

    TODO("binary descriptor")
  }

  private fun evaluateNativeFunctionDescriptor(
    descriptor: NativeFunctionDescriptor,
    environment: Environment
  ): KoflObject {
    return KoflObject.Callable.NativeFunction(nativeEnvironment, descriptor).also { function ->
      environment.declareFunction(descriptor.name, function)
    }
  }

  private fun evaluateLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor): KoflObject {
    return KoflObject.Callable.LocalFunction(this, descriptor)
  }

  private fun evaluateFunctionDescriptor(
    descriptor: FunctionDescriptor,
    environment: Environment
  ): KoflObject {
    return KoflObject.Callable.Function(this, descriptor).also { function ->
      environment.declareFunction(descriptor.name, function)
    }
  }

  private fun evaluateClassDescriptor(
    descriptor: ClassDescriptor,
    environment: Environment
  ): KoflObject {
    TODO("class descriptor")
  }

  private fun evaluateNativeDescriptor(): KoflObject {
    return KoflObject.Unit
  }

  private fun evaluateModuleDescriptor(
    descriptor: ModuleDescriptor,
    environment: Environment
  ): KoflObject {
    if (!environment.isGlobal) error("Should not exist modules in scopes that isn't the global")

    modules[descriptor.moduleName] = environment.child(descriptor)

    return KoflObject.Unit
  }

  private fun evaluateUseDescriptor(
    descriptor: UseDescriptor,
    environment: Environment
  ): KoflObject {
    val module = modules[descriptor.moduleName] ?: error(
      "Module ${descriptor.moduleName} does not exist"
    )

    environment.expand(module)

    return KoflObject.Unit
  }

  private fun assign(
    descriptor: Descriptor,
    environment: Environment,
    name: String,
    value: KoflObject
  ) {
    val distance = locals[descriptor] ?: return Unit.also {
      environment.assign(name, value)
    }

    environment.ancestor(distance).assign(name, value)
  }

  private fun lookup(descriptor: Descriptor, environment: Environment, name: String): KoflObject {
    val distance = locals[descriptor] ?: return environment.lookup(name)

    return environment.ancestor(distance).lookup(name)
  }

  private fun lookupFunction(
    descriptor: Descriptor,
    environment: Environment,
    name: String
  ): KoflObject {
    val distance = locals[descriptor] ?: return environment.lookupFunction(name)

    return environment.ancestor(distance).lookupFunction(name)
  }
}
