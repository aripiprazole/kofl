package com.lorenzoog.kofl.vm.interop

import com.lorenzoog.kofl.interpreter.internal.heap
import com.lorenzoog.kofl.interpreter.internal.mem_info
import kotlinx.cinterop.MemScope

@PublishedApi
internal val VM_HEAP = MemScope()

typealias Heap = heap
typealias MemInfo = mem_info