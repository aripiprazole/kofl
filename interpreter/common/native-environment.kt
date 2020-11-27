package com.lorenzoog.kofl.interpreter

import com.lorenzoog.kofl.frontend.*
import pw.binom.ByteBufferPool
import pw.binom.copyTo
import pw.binom.io.ByteArrayOutput
import pw.binom.io.file.File
import pw.binom.io.file.iterator
import pw.binom.io.file.read
import pw.binom.io.use

class NativeEnvironment : Environment {
  @OptIn(KoflResolverInternals::class)
  override fun get(name: Token): KoflValue = when (name.lexeme) {
    "Double" -> KoflDouble
    "Int" -> KoflInt
    "Unit" -> KoflUnit
    "Boolean" -> KoflBoolean

    "println" -> NativeFunc("println", mapOf("message" to KoflString), KoflUnit) { arguments, _ ->
      throw Return(println(arguments.entries.first().value).asKoflObject())
    }

    "print" -> NativeFunc("print", mapOf("message" to KoflString), KoflUnit) { arguments, _ ->
      throw Return(print(arguments.entries.first().value).asKoflObject())
    }

    else -> throw UnresolvedVarException(name)
  }.asKoflValue()

  override fun toString(): String = "<native env>"
}

internal fun readStdlib(): String {
  return ByteBufferPool(10).use { buffer ->
    val file = File("../stdlib/lib.kofl")

    val out = ByteArrayOutput()

    file.read().use { channel ->
      channel.copyTo(out, buffer)
    }

    out.toByteArray().decodeToString()
  }
}