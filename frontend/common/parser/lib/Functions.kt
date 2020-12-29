@file:Suppress("unused")

package com.lorenzoog.kofl.frontend.parser.lib

import com.lorenzoog.kofl.frontend.Token
import com.lorenzoog.kofl.frontend.TokenType
import com.lorenzoog.kofl.frontend.parser.grammar.line
import kotlin.jvm.JvmName

/**
 * Maps a parse function to other
 *
 * @param map functor that will map [this]
 * @return mapped parse func
 */
infix fun <T, R> Parser<T>.map(map: (T) -> R): Parser<R> = { input ->
  when (val result = this(input)) {
    is ParseResult.Error -> result.fix()
    is ParseResult.Success -> ParseResult.Success(map(result.data), result.rest)
  }
}

/**
 * Will map the func and return null if has error
 *
 * @return mapped parse func
 */
fun <T> Parser<T>.optional(): Parser<T?> = { input ->
  when (val result = this(input)) {
    is ParseResult.Success -> result.nullable()
    is ParseResult.Error -> ParseResult.Success(null as T?)
  }
}

/**
 * Will fold the function and return another parse func of [R]
 *
 * @param onSuccess functor that will map success [this]
 * @param onError functor that will map error [this]
 * @return mapped parse func
 */
fun <T, R> Parser<T>.fold(
  onSuccess: (ParseResult.Success<T>) -> ParseResult<R>,
  onError: (ParseResult.Error) -> ParseResult<R>
): Parser<R> = { input ->
  when (val result = this(input)) {
    is ParseResult.Error -> onError(result)
    is ParseResult.Success -> onSuccess(result)
  }
}

/**
 * Maps a parse function to other when result is [ParseResult.Error]
 *
 * @param map functor that will map [this]
 * @return mapped parse func
 */
infix fun <T> Parser<T>.mapErr(map: (ParseResult.Error) -> ParseResult.Error): Parser<T> = { input ->
  when (val result = this(input)) {
    is ParseResult.Error -> map(result).fix()
    is ParseResult.Success -> result
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
infix fun <T> Parser<out T>.or(another: Parser<out T>): Parser<T> {
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
fun <T> enum(vararg parsers: Parser<out T>): Parser<T> = EnumParseFunc(parsers.toList())

private class EnumParseFunc<T>(val parsers: List<Parser<out T>>) : Parser<T> {
  override fun invoke(input: String): ParseResult<T> {
    return parsers
      .map { it(input) }
      .filterIsInstance<ParseResult.Success<T>>()
      .firstOrNull()
      ?: ParseResult.Error(parsers.joinToString(), input).fix() // TODO: dump parsers correctly
  }
}

/**
 * Tries to match a text with regex [regex]
 *
 * @param regex
 * @return matched result
 */
fun regex(regex: Regex): Parser<String> = { input ->
  val match = regex.find(input)?.value

  if (match != null)
    ParseResult.Success(match, input.substring(match.length))
  else
    ParseResult.Error(regex.toString(), input).fix()
}

/**
 * Tries to match a token with regex [regex]
 *
 * @see Token
 * @param type
 * @param regex
 * @return matched result
 */
fun regex(type: TokenType, regex: Regex): Parser<Token> = { input ->
  val match = regex.findAll(input).firstOrNull()?.value

  if (match != null)
    ParseResult.Success(Token(type, match, match, line = line), input.substring(match.length))
  else
    ParseResult.Error(regex.toString(), input).fix()
}

/**
 * Tries to match a text with any [match]
 *
 * @param match
 * @return matched result
 */
fun text(match: Any) = text(match.toString())

/**
 * Tries to match a text with text [match]
 *
 * @param match
 * @return matched result
 */
fun text(match: String): Parser<String> = { input ->
  if (input.startsWith(match))
    ParseResult.Success(match, input.substring(match.length))
  else
    ParseResult.Error("'$match'", input).fix()
}

/**
 * Tries to match a token by predicate [predicate]
 *
 * @see Token
 * @param predicate
 * @return matched result
 */
fun predicate(predicate: StringMatcher): Parser<String> = { input ->
  val match = input.match(predicate)

  if (match.isNotEmpty())
    ParseResult.Success(match, input.substring(match.length))
  else
    ParseResult.Error("'$predicate'", input).fix()
}

/**
 * Tries to match a text by predicate [predicate]
 *
 * @see Token
 * @param type
 * @param predicate
 * @return matched result
 */
fun predicate(type: TokenType, predicate: StringMatcher): Parser<Token> = { input ->
  val match = input.match(predicate)

  if (match.isNotEmpty())
    ParseResult.Success(Token(type, input, input, line = line), input.substring(match.length))
  else
    ParseResult.Error("'$match'", input).fix()
}

/**
 * Tries to match a token with any [match]
 *
 * @see Token
 * @param type
 * @param match
 * @return matched result
 */
fun text(type: TokenType, match: Any) = text(type, match.toString())

/**
 * Tries to match a token with text [match]
 *
 * @see Token
 * @param type
 * @param match
 * @return matched result
 */
fun text(type: TokenType, match: String): Parser<Token> = { input ->
  if (input.startsWith(match))
    ParseResult.Success(Token(type, match, match, line = line), input.substring(match.length))
  else
    ParseResult.Error("'$match'", input).fix()
}

/**
 * Mocks a result in [success] and return it not mattering the input
 *
 * @param success mock function
 * @return mocked parse function
 */
fun <T> pure(success: () -> T): Parser<T> = { input ->
  ParseResult.Success(success(), input)
}

/**
 * Tries to get a list of [parser] and combine into a [List] of [T], if cannot,
 * returns a empty list
 *
 * @param [parser] pattern that will try to match
 * @return parse function
 */
inline fun <reified T> many(noinline parser: Parser<T>): Parser<List<T>> {
  var self: Parser<List<T>>? = null

  self = enum(
    combine(parser, lazied { self ?: error("MANY: self is null") }) { head: T, tail: List<T> ->
      listOf(head, *tail.toTypedArray())
    },
    pure { emptyList() }
  )

  return self
}

/**
 * Combines [this] with [second] into a pair of both
 *
 * @see combine
 * @param second
 * @return parse func of pair
 */
@JvmName("withAnother")
infix fun <A, B> Parser<A>.with(second: Parser<B>): Parser<Pair<A, B>> = combine(this, second) { a, b ->
  a to b
}

/**
 * Combines [this] with [third] into a triple of both
 *
 * @see combine
 * @param third
 * @return parse func of triple
 */
@JvmName("withPair")
infix fun <A, B, C> Parser<Pair<A, B>>.with(third: Parser<C>): Parser<Triple<A, B, C>> =
  combine(this, third) { (a, b), c ->
    Triple(a, b, c)
  }

/**
 * Gets lazied a parse function
 *
 * @param f lazy parse function getter
 * @return lazied parse function
 */
fun <T> lazied(f: () -> Parser<T>): Parser<T> = { input ->
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
fun <T> lexeme(junk: Parser<T>): CreateTokenParser<T> {
  return CreateTokenParser(junk)
}

class CreateTokenParser<T>(private val junk: Parser<T>) {
  operator fun <R> invoke(parser: Parser<R>): Parser<R> {
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
  operator fun <T> invoke(func: Parser<T>): Parser<T> = object : Parser<T> {
    override fun invoke(input: String): ParseResult<T> {
      return func(input).unwrapOr {
        return ParseResult.Error(label, it.actual).fix()
      }
    }

    override fun toString(): String = label
  }
}
