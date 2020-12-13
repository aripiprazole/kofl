package com.lorenzoog.kofl.vm.interop

import com.lorenzoog.kofl.interpreter.internal.value
import com.lorenzoog.kofl.interpreter.internal.value_array
import com.lorenzoog.kofl.interpreter.internal.value_array_create
import com.lorenzoog.kofl.interpreter.internal.value_array_write
import kotlinx.cinterop.*

typealias Value = value
typealias ValueArray = value_array

fun ValueArray(): CPointer<ValueArray> {
  return value_array_create(0, 0)
    ?: error("value_array_create(count = 0, capacity 0): returned null reference")
}

inline fun ValueArray.write(value: CValue<Value>) = value_array_write(ptr, value)
inline fun ValueArray.write(value: Value.() -> Unit) = write(cValue(value))
inline fun ValueArray.write(bool: Boolean) = write {
  obj._bool = bool
}

inline fun ValueArray.write(double: Double) = write {
  obj._double = double
}

inline fun ValueArray.write(int: Int) = write {
  obj._int = int
}

inline fun ValueArray.write(string: String) = write {
  obj._string = string.cstr.placeTo(VM_HEAP)
}
