package com.lorenzoog.kofl.compiler.common.backend

import com.lorenzoog.kofl.compiler.common.typing.KoflType
import com.lorenzoog.kofl.frontend.TokenType

sealed class Descriptor {
  interface Visitor<T> {
    fun visitDescriptors(descriptors: Collection<Descriptor>): Collection<T> = descriptors.map { descriptor ->
      visitDescriptor(descriptor)
    }

    fun visitDescriptor(descriptor: Descriptor): T = descriptor.accept(this)
    fun visitNativeDescriptor(descriptor: NativeDescriptor): T
    fun visitConstDescriptor(descriptor: ConstDescriptor): T
    fun visitThisDescriptor(descriptor: ThisDescriptor): T
    fun visitSetDescriptor(descriptor: SetDescriptor): T
    fun visitGetDescriptor(descriptor: GetDescriptor): T
    fun visitCallDescriptor(descriptor: CallDescriptor): T
    fun visitAccessVarDescriptor(descriptor: AccessVarDescriptor): T
    fun visitAccessFunctionDescriptor(descriptor: AccessFunctionDescriptor): T
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
    fun visitUseDescriptor(descriptor: UseDescriptor): T
    fun visitModuleDescriptor(descriptor: ModuleDescriptor): T
    fun visitLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor): T
    fun visitNativeFunctionDescriptor(descriptor: NativeFunctionDescriptor): T
    fun visitFunctionDescriptor(descriptor: FunctionDescriptor): T
    fun visitClassDescriptor(descriptor: ClassDescriptor): T
  }

  abstract val line: Int
  abstract val type: KoflType

  abstract fun <T> accept(visitor: Visitor<T>): T
}

sealed class MutableDescriptor : Descriptor() {
  abstract fun mutate(type: KoflType): Descriptor
}

object NativeDescriptor : Descriptor() {
  override val line: Int = -1
  override val type: KoflType = KoflType.Any

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitNativeDescriptor(this)
}

data class ConstDescriptor(
  val value: Any,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitConstDescriptor(this)
}

data class ThisDescriptor(
  override val line: Int,
  override val type: KoflType
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitThisDescriptor(this)
}

data class SetDescriptor(
  val receiver: Descriptor,
  val name: String,
  val value: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitSetDescriptor(this)
}

data class GetDescriptor(
  val receiver: Descriptor,
  val name: String,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitGetDescriptor(this)
}

data class CallDescriptor(
  val callee: Descriptor,
  val arguments: Map<String, Descriptor>,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitCallDescriptor(this)
}

data class AccessVarDescriptor(
  val name: String,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAccessVarDescriptor(this)
}

data class AccessFunctionDescriptor(
  val name: String,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAccessFunctionDescriptor(this)
}

data class UnaryDescriptor(
  val op: TokenType,
  val right: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUnaryDescriptor(this)
}

data class ValDescriptor(
  val name: String,
  val value: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitValDescriptor(this)
}

data class VarDescriptor(
  val name: String,
  val value: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitVarDescriptor(this)
}

data class AssignDescriptor(
  val name: String,
  val value: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitAssignDescriptor(this)
}

data class ReturnDescriptor(
  val value: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitReturnDescriptor(this)
}

data class BlockDescriptor(val body: Collection<Descriptor>, override val line: Int) : Descriptor() {
  override val type: KoflType
    get() = KoflType.Unit

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBlockDescriptor(this)
}

data class WhileDescriptor(
  val condition: Descriptor,
  val body: Collection<Descriptor>,
  override val line: Int
) : Descriptor() {
  override val type: KoflType
    get() = KoflType.Unit

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitWhileDescriptor(this)
}

data class IfDescriptor(
  val condition: Descriptor,
  val then: Collection<Descriptor>,
  val orElse: Collection<Descriptor>,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitIfDescriptor(this)
}

data class LogicalDescriptor(
  val left: Descriptor,
  val op: TokenType,
  val right: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLogicalDescriptor(this)
}

data class BinaryDescriptor(
  val left: Descriptor,
  val op: TokenType,
  val right: Descriptor,
  override val type: KoflType,
  override val line: Int
) : MutableDescriptor() {
  override fun mutate(type: KoflType): Descriptor {
    return copy(type = type)
  }

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitBinaryDescriptor(this)
}

data class UseDescriptor(val moduleName: String, override val line: Int) : Descriptor() {
  override val type: KoflType = KoflType.Unit

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitUseDescriptor(this)
}

data class ModuleDescriptor(val moduleName: String, override val line: Int) : Descriptor() {
  override val type: KoflType = KoflType.Unit

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitModuleDescriptor(this)
}

sealed class CallableDescriptor : Descriptor() {
  abstract val parameters: Map<String, KoflType>
  abstract val returnType: KoflType
}

data class LocalFunctionDescriptor(
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val body: Collection<Descriptor>,
  override val line: Int
) : CallableDescriptor() {
  override val type: KoflType
    get() = TODO("Add type to LOCAL FUNCTION DESCRIPTOR")

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitLocalFunctionDescriptor(this)
}

data class NativeFunctionDescriptor(
  val name: String,
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val nativeCall: String,
  override val line: Int
) : CallableDescriptor() {
  override val type: KoflType
    get() = TODO("Add type to NATIVE FUNCTION DESCRIPTOR")

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitNativeFunctionDescriptor(this)
}

data class FunctionDescriptor(
  val name: String,
  override val parameters: Map<String, KoflType>,
  override val returnType: KoflType,
  val body: Collection<Descriptor>,
  override val line: Int
) : CallableDescriptor() {
  override val type: KoflType
    get() = TODO("Add type to FUNCTION DESCRIPTOR")

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitFunctionDescriptor(this)
}

data class ClassDescriptor(
  val name: String,
  val inherits: Collection<KoflType>,
  val fields: Map<String, KoflType>,
  override val line: Int
) : Descriptor() {
  override val type: KoflType
    get() = TODO("Add type to CLASS DESCRIPTOR")

  override fun <T> accept(visitor: Visitor<T>): T = visitor.visitClassDescriptor(this)
}
