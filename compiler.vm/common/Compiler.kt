package me.devgabi.kofl.compiler.vm

import me.devgabi.kofl.compiler.common.backend.AccessFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.AccessVarDescriptor
import me.devgabi.kofl.compiler.common.backend.AssignDescriptor
import me.devgabi.kofl.compiler.common.backend.BinaryDescriptor
import me.devgabi.kofl.compiler.common.backend.BlockDescriptor
import me.devgabi.kofl.compiler.common.backend.CallDescriptor
import me.devgabi.kofl.compiler.common.backend.ClassDescriptor
import me.devgabi.kofl.compiler.common.backend.ConstDescriptor
import me.devgabi.kofl.compiler.common.backend.Descriptor
import me.devgabi.kofl.compiler.common.backend.FunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.GetDescriptor
import me.devgabi.kofl.compiler.common.backend.IfDescriptor
import me.devgabi.kofl.compiler.common.backend.LocalFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.LogicalDescriptor
import me.devgabi.kofl.compiler.common.backend.ModuleDescriptor
import me.devgabi.kofl.compiler.common.backend.NativeDescriptor
import me.devgabi.kofl.compiler.common.backend.NativeFunctionDescriptor
import me.devgabi.kofl.compiler.common.backend.ReturnDescriptor
import me.devgabi.kofl.compiler.common.backend.SetDescriptor
import me.devgabi.kofl.compiler.common.backend.ThisDescriptor
import me.devgabi.kofl.compiler.common.backend.UnaryDescriptor
import me.devgabi.kofl.compiler.common.backend.UseDescriptor
import me.devgabi.kofl.compiler.common.backend.ValDescriptor
import me.devgabi.kofl.compiler.common.backend.VarDescriptor
import me.devgabi.kofl.compiler.common.backend.WhileDescriptor
import me.devgabi.kofl.compiler.vm.ir.IrAccessVar
import me.devgabi.kofl.compiler.vm.ir.IrBinary
import me.devgabi.kofl.compiler.vm.ir.IrComponent
import me.devgabi.kofl.compiler.vm.ir.IrConst
import me.devgabi.kofl.compiler.vm.ir.IrContext
import me.devgabi.kofl.compiler.vm.ir.IrVal
import me.devgabi.kofl.compiler.vm.ir.IrVar
import pw.binom.ByteBuffer
import pw.binom.io.use
import pw.binom.writeInt
import pw.binom.writeUtf8Char

@ExperimentalUnsignedTypes
class Compiler(private val verbose: Boolean, private val code: List<Descriptor>) :
  Descriptor.Visitor<IrComponent> {
  fun compile(): ByteBuffer {
    val chunk = IrContext().let { context ->
      visitDescriptors(code).forEach { component ->
        component.render(context)
      }

      context.toChunk()
    }

    if (verbose) {
      println("CHUNK INFO =")
      println("  count = ${chunk.count}")
      println("  capacity = ${chunk.capacity}")
      println("  lines =")
      chunk.lines.forEach {
        println("    - $it")
      }
      print("  code = ")

      var first = true

      chunk.code.chunked(20) { code ->
        if (first) first = false else {
          print("        ")
        }

        code.forEach { op ->
          print("0x")
          print(op.toString(16))
          print(" ")
        }

        println()
      }
      println("  consts =")
      println("    count = ${chunk.consts.count}")
      println("    capacity = ${chunk.consts.capacity}")
      println("    values = ")
      chunk.consts.values.forEach { const ->
        println("      - $const")
      }
    }

    val constPoolCapacity = chunk.consts.values.fold(chunk.consts.capacity) { total, right ->
      total + right.size
    }

    val totalSize = chunk.capacity + constPoolCapacity + chunk.lines.size + 100

    return ByteBuffer.alloc(totalSize).use { buffer ->
      "kofl".forEach {
        buffer.writeUtf8Char(ByteBuffer.alloc(Char.SIZE_BITS), it)
      }

      buffer.writeChunkInfo(ChunkOp.Chunk) {
        buffer.writeChunkInfo(ChunkOp.Info) {
          buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), chunk.count)
          buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), chunk.capacity)
          buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), chunk.lines.size)
          buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), chunk.consts.capacity)
        }

        buffer.writeChunkInfo(ChunkOp.Code) {
          chunk.code.forEach { op ->
            buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), op.toInt())
          }
        }

        buffer.writeChunkInfo(ChunkOp.Lines) {
          chunk.lines.forEach { op ->
            buffer.writeInt(ByteBuffer.alloc(Int.SIZE_BITS), op)
          }
        }

        buffer.writeChunkInfo(ChunkOp.Consts) {
          chunk.consts.values.forEach { value ->
            value.write(buffer)
          }
        }
      }

      buffer.flush()

      buffer
    }
  }
/*
      OpChunk.ChunkStart, // 0

      OpChunk.InfoStart, // 1
      chunk.count.toUByte(), // 2
      chunk.capacity.toUByte(), // 3
      chunk.lines.size.toUByte(), // 4
      chunk.consts.count.toUByte(), // 5
      OpChunk.InfoEnd, // 6

      OpChunk.CodeStart, // 7
      // code.size = 2
      *chunk.code.map { it.toUByte() }.toUByteArray(), // 8
      OpChunk.CodeEnd, // 10

      OpChunk.LinesStart, // 11
      *chunk.lines.map { it.toUByte() }.toUByteArray(), // 12
      OpChunk.LinesEnd, // 13

      OpChunk.ConstsStart, // 14
      *chunk.consts.values.flatMap { it.toUByteArray() }.toUByteArray(),
      OpChunk.ConstsEnd,

      OpChunk.ChunkEnd
 */

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

  override fun visitUseDescriptor(descriptor: UseDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitModuleDescriptor(descriptor: ModuleDescriptor): IrComponent {
    TODO("Not yet implemented")
  }

  override fun visitNativeDescriptor(descriptor: NativeDescriptor): IrComponent {
    TODO("Not yet implemented")
  }
}
