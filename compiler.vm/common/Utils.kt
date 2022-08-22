package me.devgabi.kofl.compiler.vm

import pw.binom.ByteBufferPool
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.use

fun File.readContents(): ByteArray {
  return ByteBufferPool(10).use { buffer ->
    val file = File(path)

    val out = ByteArrayOutput()

    file.read().use { channel ->
      channel.copyTo(out, buffer)
    }

    out.toByteArray()
  }
}
