package com.lorenzoog.kofl.interpreter.backend

import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.interpreter.typing.KoflType

sealed class Descriptor {
  interface Visitor<T> {
    fun visitDescriptors(descriptors: Collection<Descriptor>): Collection<T> = descriptors.map { descriptor ->
      visitDescriptor(descriptor)
    }

    fun visitDescriptor(descriptor: Descriptor): T = descriptor.accept(this)
    fun visitConstDescriptor(descriptor: ConstDescriptor): T
    fun visitThisDescriptor(descriptor: ThisDescriptor): T
    fun visitSetDescriptor(descriptor: SetDescriptor): T
    fun visitGetDescriptor(descriptor: GetDescriptor): T
    fun visitCallDescriptor(descriptor: CallDescriptor): T
    fun visitGlobalVarDescriptor(descriptor: GlobalVarDescriptor): T
    fun visitUnaryDescriptor(descriptor: UnaryDescriptor): T
    fun visitValDescriptor(descriptor: ValDescriptor): T
    fun visitVarDescriptor(descriptor: VarDescriptor): T
    fun visitAssignDescriptor(descriptor: AssignDescriptor): T
    fun visitReturnDescriptor(descriptor: ReturnDescriptor): T
    fun visitBlockDescriptor(descriptor: BlockDescriptor): T
    fun visitWhileDescriptor(descriptor: WhileDescriptor): T
    fun visitIfDescriptor(descriptor: IfDescriptor): T
    fun visitLogicalDescriptor(descriptor: LogicalDescriptor): T
    fun visitBinaryDescriptor(descriptor: BinaryDescriptor): T
    fun visitLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor): T
    fun visitNativeFunctionDescriptor(descriptor: NativeFunctionDescriptor): T
    fun visitFunctionDescriptor(descriptor: FunctionDescriptor): T
    fun visitClassDescriptor(descriptor: ClassDescriptor): T
  }

  abstract fun <T> accept(visitor: Visitor<T>): T
}

data class ConstDescriptor(val value: Any, val type: KoflType) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitConstDescriptor(this)
}

class ThisDescriptor : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitThisDescriptor(this)

  override fun toString(): String = "This()"
}

data class SetDescriptor(
  val receiver: Descriptor,
  val name: String,
  val value: Descriptor,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitSetDescriptor(this)
}

data class GetDescriptor(
  val receiver: Descriptor,
  val name: String,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGetDescriptor(this)
}

data class CallDescriptor(
  val callee: Descriptor,
  val arguments: Map<String, Descriptor>,
  val type: KoflType,
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCallDescriptor(this)
}

data class GlobalVarDescriptor(val name: String) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGlobalVarDescriptor(this)
}

data class UnaryDescriptor(
  val op: TokenType,
  val right: Descriptor,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryDescriptor(this)
}

data class ValDescriptor(
  val name: String,
  val value: Descriptor,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitValDescriptor(this)
}

data class VarDescriptor(
  val name: String,
  val value: Descriptor,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarDescriptor(this)
}

data class AssignDescriptor(
  val name: String,
  val value: Descriptor,
  val type: KoflType
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignDescriptor(this)
}

data class ReturnDescriptor(val value: Descriptor, val type: KoflType) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitReturnDescriptor(this)
}

data class BlockDescriptor(val body: Collection<Descriptor>) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockDescriptor(this)
}

data class WhileDescriptor(val condition: Descriptor, val body: Collection<Descriptor>) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitWhileDescriptor(this)
}

data class IfDescriptor(
  val condition: Descriptor,
  val then: Collection<Descriptor>,
  val orElse: Collection<Descriptor>,
  val type: KoflType,
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitIfDescriptor(this)
}

data class LogicalDescriptor(
  val left: Descriptor,
  val op: TokenType,
  val right: Descriptor,
  val type: KoflType,
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLogicalDescriptor(this)
}

data class BinaryDescriptor(
  val left: Descriptor,
  val op: TokenType,
  val right: Descriptor,
  val type: KoflType,
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryDescriptor(this)
}

sealed class CallableDescriptor : Descriptor() {
  abstract val parameters: Map<String, KoflType>
  abstract val returnType: KoflType
}

data class LocalFunctionDescriptor(
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val body: Collection<Descriptor>
) : CallableDescriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLocalFunctionDescriptor(this)
}

data class NativeFunctionDescriptor(
  val name: String,
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val nativeCall: String
) : CallableDescriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitNativeFunctionDescriptor(this)
}

data class FunctionDescriptor(
  val name: String,
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val body: Collection<Descriptor>
) : CallableDescriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitFunctionDescriptor(this)
}

data class ClassDescriptor(
  val name: String,
  val inherits: Collection<KoflType>,
  val fields: Map<String, KoflType>,
) : Descriptor() {
  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitClassDescriptor(this)
}
