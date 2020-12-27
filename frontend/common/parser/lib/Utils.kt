@file:Suppress("unused")

package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Error
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Success

inline fun <T> ParseResult<T>.expect(lazyMessage: () -> String): T {
  return when (this) {
    is Error -> error(lazyMessage())
    is Success -> data
  }
}

fun <T> ParseResult<T>.unwrap(): T {
  return when (this) {
    is Error -> error("Expected: '$expected'. Actual: '$actual'")
    is Success -> data
  }
}

inline fun <T, R> ParseResult<T>.flatMap(fn: (T) -> ParseResult<R>): ParseResult<R> {
  return when (this) {
    is Error -> fix()
    is Success -> fn(data)
  }
}

inline fun <T, R> ParseResult<T>.map(fn: (T) -> R): ParseResult<R> {
  return when (this) {
    is Error -> fix()
    is Success -> Success(fn(data), rest)
  }
}

inline fun <T> ParseResult<T>.unwrapOr(def: (error: Error) -> T): Success<T> {
  return when (this) {
    is Error -> Success(def(this))
    is Success -> this
  }
}

@Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
inline fun <T> Error.fix(): ParseResult<T> {
  return this as ParseResult<T>
}
