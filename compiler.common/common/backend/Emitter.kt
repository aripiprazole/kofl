package com.lorenzoog.kofl.compiler.common.backend

class Emitter {
  private val compiled = mutableListOf<Descriptor>()

  fun emit(descriptor: Descriptor): Descriptor {
    compiled += descriptor

    return descriptor
  }

  fun compiled(): List<Descriptor> = compiled
}