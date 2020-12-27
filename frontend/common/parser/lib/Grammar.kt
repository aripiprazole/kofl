package com.lorenzoog.kofl.frontend.parser.lib

abstract class Grammar<T> {
  abstract val rule: Parser<T>

  fun parse(input: String): ParseResult<T> {
    return rule(input)
  }
}
