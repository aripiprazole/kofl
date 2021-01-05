package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.ENTER_CHAR

/**
 * The code location, that will be used for error handling
 *
 * @see Location.Code
 * @see Location.Native
 * @see Location.Offset
 */
sealed class Location {
  abstract val line: Int

  data class Code(override val line: Int, val column: Int) : Location() {
    override fun toString(): String = "($line, $column)"
  }

  data class Offset(val input: String, val index: Int) : Location() {
    override val line: Int = input.substring(0, index).split(ENTER_CHAR).size - 1

    override fun toString(): String = "($line)"
  }

  object Native : Location() {
    override val line: Int = -1

    override fun toString(): String = "(Native code)"
  }

  abstract override fun toString(): String

  companion object {
    operator fun invoke(line: Int, column: Int): Location {
      return Code(line, column)
    }
  }
}
