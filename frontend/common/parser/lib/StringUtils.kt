package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.parser.grammar.isAlpha
import com.lorenzoog.kofl.frontend.parser.grammar.isAlphaNumeric
import com.lorenzoog.kofl.frontend.parser.grammar.isDigit

typealias StringMatcher = (input: String, index: Int, current: Char) -> Boolean

fun String.match(predicate: StringMatcher): String {
  for (index in 0 until length) {
    if (!predicate(this, index, get(index))) {
      return substring(0, index)
    }
  }

  return this
}

fun String.matchString(): String? {
  if (length < 2) return null

  if (!startsWith('"')) return null

  for (index in 1 until length) {
    if (get(index) == '"') {
      return substring(1, index)
    }
  }

  return this
}

fun String.matchIdentifier(): String? {
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

fun String.matchNumeric(): String {
  return match { input, index, current ->
    current.isDigit() || (current == '.' && input.getOrNull(index + 1)?.isDigit() == true)
  }
}
