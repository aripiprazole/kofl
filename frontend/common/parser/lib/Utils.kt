@file:Suppress("unused")

package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.parser.grammar.isAlpha
import com.lorenzoog.kofl.frontend.parser.grammar.isAlphaNumeric
import com.lorenzoog.kofl.frontend.parser.grammar.isDigit
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Error
import com.lorenzoog.kofl.frontend.parser.lib.ParseResult.Success

typealias StringMatcher = (input: String, index: Int, current: Char) -> Boolean

inline fun String.match(predicate: StringMatcher): String {
  for (index in 0 until length) {
    if (!predicate(this, index, get(index))) {
      return substring(0, index)
    }
  }

  return this
}

inline fun String.matchString(): String? {
  if (length < 2) return null

  if (!startsWith('"')) return null

  for (index in 1 until length) {
    if (get(index) != '"') {
      return substring(0, index )
    }
  }

  return this
}

inline fun String.matchIdentifier(): String? {
  if (isEmpty()) return null
  if (!get(0).isAlpha()) return null

  for (index in 1 until length) {
    val value = get(index)
    if (!value.isAlphaNumeric()) {
      return substring(0, index)
    }
  }

  return this
}

inline fun String.matchNumeric(): String {
  return match { input, index, current ->
    current.isDigit() || (current == '.' && input.getOrNull(index + 1)?.isDigit() == true)
  }
}

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