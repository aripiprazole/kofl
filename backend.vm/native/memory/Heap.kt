package com.lorenzoog.kofl.vm.memory

import com.lorenzoog.kofl.interpreter.internal.heap
import com.lorenzoog.kofl.interpreter.internal.mem_info
import kotlinx.cinterop.*

@OptIn(ExperimentalUnsignedTypes::class)
fun mem_info.render(capacity: ULong) = buildString {
  append("mem_info_t(")
  append("is_free=${is_free}, ")
  append("next=${next.render(capacity)}, ")
  append("size=${size}")
  append(")")
}

@OptIn(ExperimentalUnsignedTypes::class)
fun CPointer<mem_info>?.render(capacity: ULong): String = buildString {
  if (this@render == null) return "null"

  append("[")

  var first = true
  var oldMemInfo: mem_info? = null
  var memInfo = pointed

  for (index in 0uL..capacity) {
    if (memInfo.size == 0uL) break

    if (!memInfo.isTheSame(oldMemInfo)) {
      if (first) first = false
      else append(", ")

      append(memInfo.render(capacity))
    }

    oldMemInfo = memInfo
    memInfo = this@render[index.toLong()]
  }

  append("]")
}

@OptIn(ExperimentalUnsignedTypes::class)
fun mem_info.isTheSame(memInfo: mem_info?): Boolean {
  return memInfo?.is_free == is_free && memInfo.size == memInfo.size
}

fun CPointer<heap>?.render(): String = buildString {
  if (this@render == null) return "null"

  pointed.apply {
    append("heap_t(")
    append("end=${end}, ")
    append("capacity=${capacity}, ")
    append("root=${root.render(capacity)}")
    append(")")
  }
}
