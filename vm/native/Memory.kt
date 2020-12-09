@file:OptIn(ExperimentalUnsignedTypes::class)

package com.lorenzoog.kofl.vm

inline fun growCapacity(capacity: Int): Int = if(capacity < 8) 8 else capacity * 2

