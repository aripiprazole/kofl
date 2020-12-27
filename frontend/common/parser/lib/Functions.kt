@file:Suppress("unused")

package com.lorenzoog.kofl.frontend.parser.lib

/**
 * Maps a parse function to other
 *
 * @param map functor that will map [this]
 * @return mapped parse func
 */
infix fun <T, R> ParseFunc<T>.map(map: (T) -> R): ParseFunc<R> = { input ->
  when (val result = this(input)) {
    is ParseResult.Error -> result.fix()
    is ParseResult.Success -> ParseResult.Success(map(result.data), result.rest)
  }
}

/**
 * Combines a parse function with other, if [this] is an [EnumParseFunc],
 * will flatten [this] into a enum
 *
 * @see enum
 * @param another
 * @return the combined result
 */
infix fun <T> ParseFunc<out T>.or(another: ParseFunc<out T>): ParseFunc<T> {
  if (this is EnumParseFunc) {
    return enum(*parsers.toTypedArray(), another)
  }

  return enum(this, another)
}

/**
 * Combines many [parsers] into one
 *
 * @see EnumParseFunc
 * @param [parsers]
 * @return a parse func with [parsers]
 */
fun <T> enum(vararg parsers: ParseFunc<out T>): ParseFunc<T> = EnumParseFunc(parsers.toList())

private class EnumParseFunc<T>(val parsers: List<ParseFunc<out T>>) : ParseFunc<T> {
  override fun invoke(input: String): ParseResult<T> {
    return parsers
      .map { it(input) }
      .filterIsInstance<ParseResult.Success<T>>()
      .firstOrNull()
      ?: ParseResult.Error("$parsers", input).fix()
  }
}

/**
 * Tries to match a text with regex [regex]
 *
 * @return matched result
 */
fun regex(regex: Regex): ParseFunc<String> = { input ->
  val match = regex.find(input)?.value

  if (match != null) {
    ParseResult.Success(match, input.substring(match.length))
  } else {
    ParseResult.Error(regex.toString(), input).fix()
  }
}

/**
 * Tries to match a text with any [match]
 *
 * @return matched result
 */
fun text(match: Any) = text(match.toString())

/**
 * Tries to match a text with text [match]
 *
 * @return matched result
 */
fun text(match: String): ParseFunc<String> = { input ->
  if (input.startsWith(match))
    ParseResult.Success(match, input.substring(match.length))
  else
    ParseResult.Error("'$match'", input).fix()
}

/**
 * Mocks a result in [success] and return it not mattering the input
 *
 * @param success mock function
 * @return mocked parse function
 */
fun <T> pure(success: () -> T): ParseFunc<T> = { input ->
  ParseResult.Success(success(), input)
}

/**
 * Tries to get a list of [parser] and combine into a [List] of [T], if cannot,
 * returns a empty list
 *
 * @param [parser] pattern that will try to match
 * @return parse function
 */
inline fun <reified T> many(noinline parser: ParseFunc<T>): ParseFunc<List<T>> {
  var self: ParseFunc<List<T>>? = null

  self = enum(
    combine(parser, lazied { self ?: error("MANY: self is null") }) { head: T, tail: List<T> ->
      listOf(head, *tail.toTypedArray())
    },
    pure { emptyList() }
  )

  return self
}

/**
 * Gets lazied a parse function
 *
 * @param f lazy parse function getter
 * @return lazied parse function
 */
fun <T> lazied(f: () -> ParseFunc<T>): ParseFunc<T> = { input ->
  f()(input)
}

/**
 * Creates a object that will be able to create a parse function that
 * clear the [junk]
 *
 * @see CreateTokenParser
 * @param junk
 * @return create token parser
 */
fun <T> lexeme(junk: ParseFunc<T>): CreateTokenParser<T> {
  return CreateTokenParser(junk)
}

class CreateTokenParser<T>(private val junk: ParseFunc<T>) {
  operator fun <R> invoke(parser: ParseFunc<R>): ParseFunc<R> {
    return combine(junk, parser, junk) { _, first, _ ->
      first
    }
  }
}

/**
 * Creates a object that will label a parse function for easier
 * debug
 *
 * @see CreateLabelParser
 * @param label
 * @return create label parser
 */
fun label(label: String): CreateLabelParser {
  return CreateLabelParser(label)
}

class CreateLabelParser(private val label: String) {
  operator fun <T> invoke(func: ParseFunc<T>): ParseFunc<T> = func@{ input ->
    func(input).unwrapOr {
      return@func ParseResult.Error(label, it.actual).fix()
    }
  }
}
