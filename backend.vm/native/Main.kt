package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.internal.*
import com.lorenzoog.kofl.vm.memory.render
import kotlinx.cinterop.*

@OptIn(ExperimentalUnsignedTypes::class)
fun main(): Unit = memScoped {
  val heap = create_heap(512_000)

  val pointer = heap_alloc(heap, sizeOf<IntVar>().toULong())?.reinterpret<IntVar>()?.apply {
    pointed.value = 34
  }

  println("HEAP=${heap.render()}")
  println("INTVAR=${pointer?.pointed?.value}")

  heap_free(heap, pointer)
}