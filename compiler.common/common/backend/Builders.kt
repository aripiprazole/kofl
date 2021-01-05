// Generated code. Please don't edit.
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.compiler.common.backend

import com.lorenzoog.kofl.compiler.common.typing.KfType
import com.lorenzoog.kofl.frontend.TokenType
import kotlin.DslMarker
import kotlin.Suppress
import kotlin.Unit

@DslMarker
annotation class DescriptorDsl

class ConstDescriptorBuilder {
  var value: Any? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): ConstDescriptor = ConstDescriptor(value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun constDescriptor(builder: ConstDescriptorBuilder.() -> Unit): ConstDescriptor =
    ConstDescriptorBuilder().apply(builder).build()

class ThisDescriptorBuilder {
  var line: Int? = null

  var type: KfType? = null

  fun build(): ThisDescriptor = ThisDescriptor(line!!, type!!, )
}

@DescriptorDsl
inline fun thisDescriptor(builder: ThisDescriptorBuilder.() -> Unit): ThisDescriptor =
    ThisDescriptorBuilder().apply(builder).build()

class SetDescriptorBuilder {
  var receiver: Descriptor? = null

  var name: String? = null

  var value: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): SetDescriptor = SetDescriptor(receiver!!, name!!, value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun setDescriptor(builder: SetDescriptorBuilder.() -> Unit): SetDescriptor =
    SetDescriptorBuilder().apply(builder).build()

class GetDescriptorBuilder {
  var receiver: Descriptor? = null

  var name: String? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): GetDescriptor = GetDescriptor(receiver!!, name!!, type!!, line!!, )
}

@DescriptorDsl
inline fun getDescriptor(builder: GetDescriptorBuilder.() -> Unit): GetDescriptor =
    GetDescriptorBuilder().apply(builder).build()

class CallDescriptorBuilder {
  var callee: Descriptor? = null

  var arguments: Map<String, Descriptor>? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): CallDescriptor = CallDescriptor(callee!!, arguments!!, type!!, line!!, )
}

@DescriptorDsl
inline fun callDescriptor(builder: CallDescriptorBuilder.() -> Unit): CallDescriptor =
    CallDescriptorBuilder().apply(builder).build()

class AccessVarDescriptorBuilder {
  var name: String? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): AccessVarDescriptor = AccessVarDescriptor(name!!, type!!, line!!, )
}

@DescriptorDsl
inline fun accessVarDescriptor(builder: AccessVarDescriptorBuilder.() -> Unit): AccessVarDescriptor
    = AccessVarDescriptorBuilder().apply(builder).build()

class AccessFunctionDescriptorBuilder {
  var name: String? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): AccessFunctionDescriptor = AccessFunctionDescriptor(name!!, type!!, line!!, )
}

@DescriptorDsl
inline fun accessFunctionDescriptor(builder: AccessFunctionDescriptorBuilder.() -> Unit):
    AccessFunctionDescriptor = AccessFunctionDescriptorBuilder().apply(builder).build()

class UnaryDescriptorBuilder {
  var op: TokenType? = null

  var right: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): UnaryDescriptor = UnaryDescriptor(op!!, right!!, type!!, line!!, )
}

@DescriptorDsl
inline fun unaryDescriptor(builder: UnaryDescriptorBuilder.() -> Unit): UnaryDescriptor =
    UnaryDescriptorBuilder().apply(builder).build()

class ValDescriptorBuilder {
  var name: String? = null

  var value: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): ValDescriptor = ValDescriptor(name!!, value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun valDescriptor(builder: ValDescriptorBuilder.() -> Unit): ValDescriptor =
    ValDescriptorBuilder().apply(builder).build()

class VarDescriptorBuilder {
  var name: String? = null

  var value: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): VarDescriptor = VarDescriptor(name!!, value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun varDescriptor(builder: VarDescriptorBuilder.() -> Unit): VarDescriptor =
    VarDescriptorBuilder().apply(builder).build()

class AssignDescriptorBuilder {
  var name: String? = null

  var value: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): AssignDescriptor = AssignDescriptor(name!!, value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun assignDescriptor(builder: AssignDescriptorBuilder.() -> Unit): AssignDescriptor =
    AssignDescriptorBuilder().apply(builder).build()

class ReturnDescriptorBuilder {
  var value: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): ReturnDescriptor = ReturnDescriptor(value!!, type!!, line!!, )
}

@DescriptorDsl
inline fun returnDescriptor(builder: ReturnDescriptorBuilder.() -> Unit): ReturnDescriptor =
    ReturnDescriptorBuilder().apply(builder).build()

class BlockDescriptorBuilder {
  var body: Collection<Descriptor>? = null

  var line: Int? = null

  fun build(): BlockDescriptor = BlockDescriptor(body!!, line!!, )
}

@DescriptorDsl
inline fun blockDescriptor(builder: BlockDescriptorBuilder.() -> Unit): BlockDescriptor =
    BlockDescriptorBuilder().apply(builder).build()

class WhileDescriptorBuilder {
  var condition: Descriptor? = null

  var body: Collection<Descriptor>? = null

  var line: Int? = null

  fun build(): WhileDescriptor = WhileDescriptor(condition!!, body!!, line!!, )
}

@DescriptorDsl
inline fun whileDescriptor(builder: WhileDescriptorBuilder.() -> Unit): WhileDescriptor =
    WhileDescriptorBuilder().apply(builder).build()

class IfDescriptorBuilder {
  var condition: Descriptor? = null

  var then: Collection<Descriptor>? = null

  var orElse: Collection<Descriptor>? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): IfDescriptor = IfDescriptor(condition!!, then!!, orElse!!, type!!, line!!, )
}

@DescriptorDsl
inline fun ifDescriptor(builder: IfDescriptorBuilder.() -> Unit): IfDescriptor =
    IfDescriptorBuilder().apply(builder).build()

class LogicalDescriptorBuilder {
  var left: Descriptor? = null

  var op: TokenType? = null

  var right: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): LogicalDescriptor = LogicalDescriptor(left!!, op!!, right!!, type!!, line!!, )
}

@DescriptorDsl
inline fun logicalDescriptor(builder: LogicalDescriptorBuilder.() -> Unit): LogicalDescriptor =
    LogicalDescriptorBuilder().apply(builder).build()

class BinaryDescriptorBuilder {
  var left: Descriptor? = null

  var op: TokenType? = null

  var right: Descriptor? = null

  var type: KfType? = null

  var line: Int? = null

  fun build(): BinaryDescriptor = BinaryDescriptor(left!!, op!!, right!!, type!!, line!!, )
}

@DescriptorDsl
inline fun binaryDescriptor(builder: BinaryDescriptorBuilder.() -> Unit): BinaryDescriptor =
    BinaryDescriptorBuilder().apply(builder).build()

class UseDescriptorBuilder {
  var moduleName: String? = null

  var line: Int? = null

  fun build(): UseDescriptor = UseDescriptor(moduleName!!, line!!, )
}

@DescriptorDsl
inline fun useDescriptor(builder: UseDescriptorBuilder.() -> Unit): UseDescriptor =
    UseDescriptorBuilder().apply(builder).build()

class ModuleDescriptorBuilder {
  var moduleName: String? = null

  var line: Int? = null

  fun build(): ModuleDescriptor = ModuleDescriptor(moduleName!!, line!!, )
}

@DescriptorDsl
inline fun moduleDescriptor(builder: ModuleDescriptorBuilder.() -> Unit): ModuleDescriptor =
    ModuleDescriptorBuilder().apply(builder).build()

class LocalFunctionDescriptorBuilder {
  var parameters: Map<String, KfType>? = null

  var returnType: KfType? = null

  var body: Collection<Descriptor>? = null

  var line: Int? = null

  fun build(): LocalFunctionDescriptor = LocalFunctionDescriptor(parameters!!, returnType!!, body!!,
      line!!, )
}

@DescriptorDsl
inline fun localFunctionDescriptor(builder: LocalFunctionDescriptorBuilder.() -> Unit):
    LocalFunctionDescriptor = LocalFunctionDescriptorBuilder().apply(builder).build()

class NativeFunctionDescriptorBuilder {
  var name: String? = null

  var parameters: Map<String, KfType>? = null

  var returnType: KfType? = null

  var nativeCall: String? = null

  var line: Int? = null

  fun build(): NativeFunctionDescriptor = NativeFunctionDescriptor(name!!, parameters!!,
      returnType!!, nativeCall!!, line!!, )
}

@DescriptorDsl
inline fun nativeFunctionDescriptor(builder: NativeFunctionDescriptorBuilder.() -> Unit):
    NativeFunctionDescriptor = NativeFunctionDescriptorBuilder().apply(builder).build()

class FunctionDescriptorBuilder {
  var name: String? = null

  var parameters: Map<String, KfType>? = null

  var returnType: KfType? = null

  var body: Collection<Descriptor>? = null

  var line: Int? = null

  fun build(): FunctionDescriptor = FunctionDescriptor(name!!, parameters!!, returnType!!, body!!,
      line!!, )
}

@DescriptorDsl
inline fun functionDescriptor(builder: FunctionDescriptorBuilder.() -> Unit): FunctionDescriptor =
    FunctionDescriptorBuilder().apply(builder).build()

class ClassDescriptorBuilder {
  var name: String? = null

  var inherits: Collection<KfType>? = null

  var fields: Map<String, KfType>? = null

  var line: Int? = null

  fun build(): ClassDescriptor = ClassDescriptor(name!!, inherits!!, fields!!, line!!, )
}

@DescriptorDsl
inline fun classDescriptor(builder: ClassDescriptorBuilder.() -> Unit): ClassDescriptor =
    ClassDescriptorBuilder().apply(builder).build()
