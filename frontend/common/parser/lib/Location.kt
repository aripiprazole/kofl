package com.lorenzoog.kofl.frontend.parser.lib

/**
 * The code location, that will be used for error handling
 *
 * @see Location.Code
 * @see Location.Labeled
 * @see Location.Native
 */
sealed class Location {
  data class Code(val line: Int, val column: Int) : Location() {
    override fun toString(): String = "($line, $column)"
  }

  data class Labeled(val label: String) {
    override fun toString(): String = "($label)"
  }

  object Native : Location() {
    override fun toString(): String = "(Native code)"
  }

  abstract override fun toString(): String

  companion object {
    operator fun invoke(line: Int, column: Int): Location {
      return Code(line, column)
    }
  }
}
