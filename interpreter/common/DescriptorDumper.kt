package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.compiler.common.backend.*
import com.lorenzoog.kofl.compiler.common.typing.KfType

internal fun Descriptor?.dump(): String = when (this) {
  is ConstDescriptor -> dump()
  is ThisDescriptor -> TODO("THIS DESCRIPTOR dumper")
  is SetDescriptor -> dump()
  is GetDescriptor -> dump()
  is CallDescriptor -> dump()
  is AccessVarDescriptor -> dump()
  is AccessFunctionDescriptor -> dump()
  is UnaryDescriptor -> dump()
  is ValDescriptor -> dump()
  is VarDescriptor -> dump()
  is AssignDescriptor -> dump()
  is ReturnDescriptor -> dump()
  is BlockDescriptor -> dump()
  is WhileDescriptor -> dump()
  is IfDescriptor -> dump()
  is LogicalDescriptor -> dump()
  is BinaryDescriptor -> dump()
  is LocalFunctionDescriptor -> dump()
  is NativeFunctionDescriptor -> dump()
  is FunctionDescriptor -> dump()
  is ClassDescriptor -> dump()
  else -> "root"
}

private inline fun ConstDescriptor.dump(): String = when (value) {
  is String -> "\"$value\""
  else -> "$value"
}

private inline fun SetDescriptor.dump(): String {
  return "set ${receiver.dump()}.$name ${value.dump()}: ${KfType.Unit}"
}

private inline fun GetDescriptor.dump(): String {
  return "get ${receiver.dump()}.$name: $type"
}

private inline fun CallDescriptor.dump(): String = buildString {
  append("call ")
  append(callee.dump())
  append(arguments.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
    "$name: ${type.dump()}"
  })
  append(": $type")
}

private inline fun AccessVarDescriptor.dump(): String {
  return name
}

private inline fun AccessFunctionDescriptor.dump(): String {
  return name
}

private inline fun UnaryDescriptor.dump(): String {
  return "($op ${right.dump()})"
}

private inline fun ValDescriptor.dump(): String {
  return "val $name ${value.dump()}"
}

private inline fun VarDescriptor.dump(): String {
  return "var $name ${value.dump()}"
}

private inline fun AssignDescriptor.dump(): String {
  return "assign $name ${value.dump()}"
}

private inline fun ReturnDescriptor.dump(): String {
  return "return ${value.dump()}"
}

private inline fun BlockDescriptor.dump(): String {
  return "block $type"
}

private inline fun WhileDescriptor.dump(): String {
  return "while ${condition.dump()}"
}

private inline fun IfDescriptor.dump(): String {
  return "if ${condition.dump()}"
}

private inline fun LogicalDescriptor.dump(): String {
  return "${left.dump()} $op ${right.dump()}"
}

private inline fun BinaryDescriptor.dump(): String {
  return "${left.dump()} $op ${right.dump()}"
}

private inline fun CallableDescriptor.dump(): String = buildString {
  append("func ")
  append(parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
    "$name: $type"
  })
  append(": ")
  append(returnType)
}

private inline fun ClassDescriptor.dump(): String = buildString {
  append("type class ")
  append(name)
}