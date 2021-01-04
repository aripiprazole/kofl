package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Error
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Success

typealias Parser<T> = (Context) -> ParseResult<T>

sealed class ParseResult<T> {
  data class Success<T>(
    val data: T,
    val rest: Context = EmptyContext
  ) : ParseResult<T>() {
    override fun toString(): String = "Success(data=$data, rest=$rest)"
  }

  data class Error(
    val expected: String,
    val actual: Context,
    val location: Location? = null
  ) : ParseResult<Nothing>()
}

inline fun <T> ParseResult<T>.expect(lazyMessage: () -> String): T {
  return when (this) {
    is Error -> error(lazyMessage())
    is Success -> data
  }
}

fun <T> Parser<T>.parse(input: String) = this(EmptyContext.copy(input = input))

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

inline fun <T> ParseResult<T>.nullable(): ParseResult<T?> {
  return map { it }
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

fun <T> identity(t: T): () -> T {
  return { t }
}
