package com.lorenzoog.kofl.vm.interop

import com.lorenzoog.kofl.interpreter.internal.*
import kotlinx.cinterop.*

typealias Value = value
typealias ValueType = value_type
typealias ValueArray = value_array

inline fun Value(type: ValueType, builder: obj_as_t.() -> Unit): CValue<Value> {
  return cValue {
    this.type = type
    obj.builder()
  }
}

fun ValueArray(): CPointer<ValueArray> {
  return value_array_create(0, 0)
    ?: error("value_array_create(count = 0, capacity 0): returned null reference")
}

inline fun ValueArray.write(value: CValue<Value>) = value_array_write(ptr, value)
