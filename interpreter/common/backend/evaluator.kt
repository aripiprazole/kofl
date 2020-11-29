package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.interpreter.exceptions.KoflRuntimeException

class Evaluator(private val locals: MutableMap<Descriptor, Int> = mutableMapOf()) {
  private val globalEnvironment = Environment()

  fun evaluate(
    descriptors: Collection<Descriptor>,
    environment: Environment = globalEnvironment
  ): Collection<KoflObject> = descriptors.map {
    evaluate(it, environment)
  }

  fun evaluate(descriptor: Descriptor, environment: Environment = globalEnvironment): KoflObject = when (descriptor) {
    is ConstDescriptor -> evaluateConstDescriptor(descriptor)
    is ThisDescriptor -> evaluateThisDescriptor(descriptor, environment)
    is SetDescriptor -> evaluateSetDescriptor(descriptor, environment)
    is GetDescriptor -> evaluateGetDescriptor(descriptor, environment)
    is CallDescriptor -> evaluateCallDescriptor(descriptor, environment)
    is GlobalVarDescriptor -> evaluateGlobalVarDescriptor(descriptor, environment)
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
  }

  private fun evaluateConstDescriptor(descriptor: ConstDescriptor): KoflObject {
    return KoflObject(descriptor.value)
  }

  private fun evaluateThisDescriptor(descriptor: ThisDescriptor, environment: Environment): KoflObject {
    return lookup(descriptor, environment)
  }

  private fun evaluateGlobalVarDescriptor(descriptor: GlobalVarDescriptor, environment: Environment): KoflObject {
    return lookup(descriptor, environment)
  }

  private fun evaluateGetDescriptor(descriptor: GetDescriptor, environment: Environment): KoflObject {
    TODO("get descriptor")
  }

  private fun evaluateSetDescriptor(descriptor: SetDescriptor, environment: Environment): KoflObject {
    TODO("set descriptor")
  }

  private fun evaluateUnaryDescriptor(descriptor: UnaryDescriptor, environment: Environment): KoflObject {
    val op = descriptor.op
    val right = evaluate(descriptor.right, environment)

    TODO("unary descriptor")
  }

  private fun evaluateCallDescriptor(descriptor: CallDescriptor, environment: Environment): KoflObject {
    val callee = evaluate(descriptor, environment)
    val arguments = descriptor.arguments.mapValues { (_, value) ->
      evaluate(value, environment)
    }

    if (callee !is KoflObject.Callable)
      throw KoflRuntimeException.InvalidType(KoflObject.Callable::class, callee)

    return callee(arguments, environment)
  }

  private fun evaluateValDescriptor(descriptor: ValDescriptor, environment: Environment): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      environment.declare(descriptor.name, Value.Immutable(data))
    }
  }

  private fun evaluateVarDescriptor(descriptor: VarDescriptor, environment: Environment): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      environment.declare(descriptor.name, Value.Mutable(data))
    }
  }

  private fun evaluateAssignDescriptor(descriptor: AssignDescriptor, environment: Environment): KoflObject {
    return evaluate(descriptor.value, environment).also { data ->
      assign(descriptor, environment, descriptor.name, data)
    }
  }

  private fun evaluateReturnDescriptor(descriptor: ReturnDescriptor, environment: Environment): KoflObject {
    throw ReturnException(evaluate(descriptor.value, environment))
  }

  private fun evaluateBlockDescriptor(descriptor: BlockDescriptor, environment: Environment): KoflObject {
    TODO("block descriptor")
  }

  private fun evaluateWhileDescriptor(descriptor: WhileDescriptor, environment: Environment): KoflObject {
    TODO("while descriptor")
  }

  private fun evaluateIfDescriptor(descriptor: IfDescriptor, environment: Environment): KoflObject {
    val condition = evaluate(descriptor.condition, environment)
    val then = evaluate(descriptor.then, environment)
    val orElse = evaluate(descriptor.orElse, environment)

    TODO("if descriptor")
  }

  private fun evaluateLogicalDescriptor(descriptor: LogicalDescriptor, environment: Environment): KoflObject {
    val op = descriptor.op
    val left = evaluate(descriptor.left, environment).unwrap()
    val right = evaluate(descriptor.left, environment).unwrap()

    TODO("logical descriptor")
  }

  private fun evaluateBinaryDescriptor(descriptor: BinaryDescriptor, environment: Environment): KoflObject {
    val op = descriptor.op
    val left = evaluate(descriptor.left).unwrap()
    val right = evaluate(descriptor.left).unwrap()

    TODO("binary descriptor")
  }

  private fun evaluateNativeFunctionDescriptor(
    descriptor: NativeFunctionDescriptor,
    environment: Environment
  ): KoflObject {
    return KoflObject.Callable.NativeFunction(descriptor).also { function ->
      environment.declareFunction(descriptor.name, function)
    }
  }

  private fun evaluateLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor): KoflObject {
    return KoflObject.Callable.LocalFunction(this, descriptor)
  }

  private fun evaluateFunctionDescriptor(descriptor: FunctionDescriptor, environment: Environment): KoflObject {
    return KoflObject.Callable.Function(this, descriptor).also { function ->
      environment.declareFunction(descriptor.name, function)
    }
  }

  private fun evaluateClassDescriptor(descriptor: ClassDescriptor, environment: Environment): KoflObject {
    TODO("class descriptor")
  }

  private fun assign(descriptor: Descriptor, environment: Environment, name: String, value: KoflObject) {
    TODO("assign")
  }

  private fun lookup(descriptor: Descriptor, environment: Environment): KoflObject {
    TODO("lookup")
  }
}