package com.lorenzoog.kofl.frontend.parser.lib

abstract class Grammar<T> : Parser<T> {
  protected abstract val rule: Parser<T>

  fun parse(input: String): ParseResult<T> {
    return rule(Context(input))
  }

  override fun invoke(input: Context): ParseResult<T> {
    return rule(input)
  }

  override fun toString(): String {
    return rule.toString()
  }
}
