package com.lorenzoog.kofl.vm.memory

import com.lorenzoog.kofl.vm.interop.Heap
import com.lorenzoog.kofl.vm.interop.MemInfo
import kotlinx.cinterop.*

@OptIn(ExperimentalUnsignedTypes::class)
fun MemInfo.render(capacity: ULong) = buildString {
  append("mem_info_t(")
  append("is_free=${is_free}, ")
  append("next=${next.render(capacity)}, ")
  append("size=${size}")
  append(")")
}

@OptIn(ExperimentalUnsignedTypes::class)
fun CPointer<MemInfo>?.render(capacity: ULong): String = buildString {
  if (this@render == null) return "null"

  append("[")

  var first = true
  var oldMemInfo: MemInfo? = null
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
fun MemInfo.isTheSame(memInfo: MemInfo?): Boolean {
  return memInfo?.is_free == is_free && memInfo.size == memInfo.size
}

fun CPointer<Heap>?.render(): String = buildString {
  if (this@render == null) return "null"

  pointed.apply {
    append("heap_t(")
    append("end=${end}, ")
    append("capacity=${capacity}, ")
    append("root=${root.render(capacity)}")
    append(")")
  }
}
