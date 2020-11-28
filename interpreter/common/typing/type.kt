package com.lorenzoog.kofl.interpreter.typing

sealed class KoflType {
  class Function(
    val parameters: Map<String, KoflType>,
    val returnType: KoflType,
    val receiver: KoflType? = null
  ) : KoflType()

  class Primitive private constructor() : KoflType() {
    companion object {
      val Any = Primitive()
      val String = Primitive()
      val Unit = Primitive()
      val Double = Primitive()
      val Int = Primitive()
      val Boolean = Primitive()
    }
  }
}

