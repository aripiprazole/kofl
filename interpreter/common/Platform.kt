package com.lorenzoog.kofl.interpreter

import pw.binom.ByteBufferPool
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.use

expect object Platform {
  val stdlibPath: String
}

expect fun exit(code: Int)

internal fun readStdlib(path: String): String {
  return ByteBufferPool(10).use { buffer ->
    val file = File(path)

    val out = ByteArrayOutput()

    file.read().use { channel ->
      channel.copyTo(out, buffer)
    }

    out.toByteArray().decodeToString()
  }
}
