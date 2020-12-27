package com.lorenzoog.kofl.frontend.parser.lib

abstract class Grammar<T> {
  protected abstract val rule: ParseFunc<T>

  fun parse(input: String): ParseResult<T> {
    return rule(input)
  }
}
