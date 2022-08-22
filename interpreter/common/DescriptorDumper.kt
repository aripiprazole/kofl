package me.devgabi.kofl.interpreter

import me.devgabi.kofl.compiler.common.backend.AccessFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.AccessVarDescriptor
import me.devgabi.kofl.compiler.common.backend.AssignDescriptor
import me.devgabi.kofl.compiler.common.backend.BinaryDescriptor
import me.devgabi.kofl.compiler.common.backend.BlockDescriptor
import me.devgabi.kofl.compiler.common.backend.CallDescriptor
import me.devgabi.kofl.compiler.common.backend.CallableDescriptor
import me.devgabi.kofl.compiler.common.backend.ClassDescriptor
import me.devgabi.kofl.compiler.common.backend.ConstDescriptor
import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.backend.FunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.GetDescriptor
import me.devgabi.kofl.compiler.common.backend.IfDescriptor
import me.devgabi.kofl.compiler.common.backend.LocalFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.LogicalDescriptor
import me.devgabi.kofl.compiler.common.backend.NativeFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.ReturnDescriptor
import me.devgabi.kofl.compiler.common.backend.SetDescriptor
import me.devgabi.kofl.compiler.common.backend.ThisDescriptor
import me.devgabi.kofl.compiler.common.backend.UnaryDescriptor
import me.devgabi.kofl.compiler.common.backend.ValDescriptor
import me.devgabi.kofl.compiler.common.backend.VarDescriptor
import me.devgabi.kofl.compiler.common.backend.WhileDescriptor
import me.devgabi.kofl.compiler.common.typing.KfType

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

private fun ConstDescriptor.dump(): String = when (value) {
  is String -> "\"$value\""
  else -> "$value"
}

private fun SetDescriptor.dump(): String {
  return "set ${receiver.dump()}.$name ${value.dump()}: ${KfType.Unit}"
}

private fun GetDescriptor.dump(): String {
  return "get ${receiver.dump()}.$name: $type"
}

private fun CallDescriptor.dump(): String = buildString {
  append("call ")
  append(callee.dump())
  append(
    arguments.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
      "$name: ${type.dump()}"
    }
  )
  append(": $type")
}

private fun AccessVarDescriptor.dump(): String {
  return name
}

private fun AccessFunctionDescriptor.dump(): String {
  return name
}

private fun UnaryDescriptor.dump(): String {
  return "($op ${right.dump()})"
}

private fun ValDescriptor.dump(): String {
  return "val $name ${value.dump()}"
}

private fun VarDescriptor.dump(): String {
  return "var $name ${value.dump()}"
}

private fun AssignDescriptor.dump(): String {
  return "assign $name ${value.dump()}"
}

private fun ReturnDescriptor.dump(): String {
  return "return ${value.dump()}"
}

private fun BlockDescriptor.dump(): String {
  return "block $type"
}

private fun WhileDescriptor.dump(): String {
  return "while ${condition.dump()}"
}

private fun IfDescriptor.dump(): String {
  return "if ${condition.dump()}"
}

private fun LogicalDescriptor.dump(): String {
  return "${left.dump()} $op ${right.dump()}"
}

private fun BinaryDescriptor.dump(): String {
  return "${left.dump()} $op ${right.dump()}"
}

private fun CallableDescriptor.dump(): String = buildString {
  append("func ")
  append(
    parameters.entries.joinToString(prefix = "(", postfix = ")") { (name, type) ->
      "$name: $type"
    }
  )
  append(": ")
  append(returnType)
}

private fun ClassDescriptor.dump(): String = buildString {
  append("type class ")
  append(name)
}
