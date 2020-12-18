@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.compiler.vm

import com.lorenzoog.kofl.compiler.common.backend.*
import com.lorenzoog.kofl.compiler.vm.ir.*

class Compiler(private val code: List<Descriptor>) : Descriptor.Visitor<IrComponent> {
  @OptIn(ExperimentalUnsignedTypes::class)
  fun compile(): UByteArray {
    val chunk = IrContext().let { context ->
      visitDescriptors(code).forEach {
        it.render(context)
      }

      context.toChunk()
    }

    return ubyteArrayOf(
      OpChunk.ChunkStart,

      OpChunk.InfoStart,
      chunk.count.toUByte(),
      chunk.capacity.toUByte(),
      OpChunk.InfoEnd,

      OpChunk.CodeStart,
      *chunk.code.map { it.toUByte() }.toUByteArray(),
      OpChunk.CodeEnd,

      OpChunk.LinesStart,
      *chunk.lines.map { it.toUByte() }.toUByteArray(),
      OpChunk.LinesEnd,

      OpChunk.ConstsStart,
      *chunk.consts.values.flatMap { it.toUByteArray() }.toUByteArray(),
      OpChunk.ConstsEnd,

      OpChunk.ChunkEnd
    )
  }

  override fun visitConstDescriptor(descriptor: ConstDescriptor): IrComponent {
    return IrConst(descriptor.value, descriptor.type, descriptor.line)
  }

  override fun visitThisDescriptor(descriptor: ThisDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitSetDescriptor(descriptor: SetDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitGetDescriptor(descriptor: GetDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitCallDescriptor(descriptor: CallDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitAccessVarDescriptor(descriptor: AccessVarDescriptor): IrComponent {
    return IrAccessVar(descriptor.name, descriptor.line)
  }

  override fun visitAccessFunctionDescriptor(descriptor: AccessFunctionDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitUnaryDescriptor(descriptor: UnaryDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitValDescriptor(descriptor: ValDescriptor): IrComponent {
    return IrVal(descriptor.name, visitDescriptor(descriptor.value), descriptor.line)
  }

  override fun visitVarDescriptor(descriptor: VarDescriptor): IrComponent {
    return IrVar(descriptor.name, visitDescriptor(descriptor.value), descriptor.line)
  }

  override fun visitAssignDescriptor(descriptor: AssignDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitReturnDescriptor(descriptor: ReturnDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitBlockDescriptor(descriptor: BlockDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitWhileDescriptor(descriptor: WhileDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitIfDescriptor(descriptor: IfDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitLogicalDescriptor(descriptor: LogicalDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitBinaryDescriptor(descriptor: BinaryDescriptor): IrComponent {
    return IrBinary(
      visitDescriptor(descriptor.left),
      visitDescriptor(descriptor.right),
      descriptor.op,
      descriptor.line
    )
  }

  override fun visitLocalFunctionDescriptor(descriptor: LocalFunctionDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitNativeFunctionDescriptor(descriptor: NativeFunctionDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitFunctionDescriptor(descriptor: FunctionDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitClassDescriptor(descriptor: ClassDescriptor): IrComponent {
    TODO("Not yet implemented")
  }
}
