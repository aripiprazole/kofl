package com.lorenzoog.kofl.vm.interop

import com.lorenzoog.kofl.interpreter.internal.vm
import com.lorenzoog.kofl.interpreter.internal.vm_create
import com.lorenzoog.kofl.interpreter.internal.vm_eval
import kotlinx.cinterop.cValue
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr

typealias Vm = vm

@OptIn(ExperimentalUnsignedTypes::class)
fun Vm(verbose: Boolean, memory: Int): Vm {
  return vm_create(cValue {
    this.verbose = verbose
    this.memory = memory.toULong()
  })?.pointed ?: error("vm_create(flags = {verbose=$verbose, memory=$memory}): returned null referene")
}

fun Vm.eval(chunk: Chunk) = vm_eval(ptr, chunk.ptr)