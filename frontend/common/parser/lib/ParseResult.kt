package com.lorenzoog.kofl.frontend.parser.lib

typealias Parser<T> = (input: String) -> ParseResult<T>

sealed class ParseResult<T> {
  data class Success<T>(
    val data: T,
    val rest: String = ""
  ) : ParseResult<T>() {
    override fun toString(): String = "Success(data=$data, rest='$rest')"
  }

  data class Error(
    val expected: String,
    val actual: String,
    val location: Location? = null
  ) : ParseResult<Nothing>()
}
