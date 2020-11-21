val RESERVED_KEYWORDS = mapOf(
  "struct" to TokenType.Struct,
  "typedef" to TokenType.Typedef,
  "func" to TokenType.Func,
  "this" to TokenType.This,
  "if" to TokenType.If,
  "else" to TokenType.Else,
  "false" to TokenType.False,
  "true" to TokenType.True,
  "val" to TokenType.Val,
  "var" to TokenType.Var,
  "return" to TokenType.Return,
  "while" to TokenType.While,
  "extern" to TokenType.Extern,
)

class Scanner(private val source: String) {
  private val tokens = mutableListOf<Token>()
  private val isAtEnd get() = current >= source.length

  private var start = 0
  private var current = 0
  private var line = 1

  fun scan(): List<Token> {
    while (!isAtEnd) {
      start = current
      scanToken()
    }

    addToken(TokenType.Eof)

    return tokens
  }

  private fun scanToken() {
    when (val char = advance()) {
      // single-character tokens
      '(' -> addToken(TokenType.LeftParen)
      ')' -> addToken(TokenType.RightParen)
      '{' -> addToken(TokenType.LeftBrace)
      '}' -> addToken(TokenType.RightBrace)
      '.' -> addToken(TokenType.Dot)
      ',' -> addToken(TokenType.Comma)
      ';' -> addToken(TokenType.Semicolon)
      ':' -> addToken(TokenType.Colon)
      '@' -> addToken(TokenType.At)

      // one-or-double-character tokens
      '!' -> addToken(if (match('=')) TokenType.BangEqual else TokenType.Bang)
      '>' -> addToken(if (match('=')) TokenType.GreaterEqual else TokenType.Greater)
      '<' -> addToken(if (match('=')) TokenType.LessEqual else TokenType.Less)
      '=' -> addToken(if (match('=')) TokenType.EqualEqual else TokenType.Equal)

      // comments and assign characters
      '/' -> when {
        match('/') -> while (peek() != ENTER_CHAR && !isAtEnd) advance()
        match('*') -> scanMultilineComments()
        else -> addToken(TokenType.Slash)
      }

      '+' -> addToken(TokenType.Plus)
      '-' -> addToken(TokenType.Minus)
      '*' -> addToken(TokenType.Star)

      // string characters
      '"' -> scanString()

      // reserved keywords
      // TODO: add bitwise tokens
      '&' -> addToken(
        if (match('&')) TokenType.And else
          throw LexError(line, "&", "Unfinished ${TokenType.And} expression")
      )
      '|' -> addToken(
        if (match('|')) TokenType.And else
          throw LexError(line, "|", "Unfinished ${TokenType.Or} expression")
      )

      // useless characters
      SPACE_CHAR, WINDOWS_ENTER_CHAR, TAB_CHAR -> {
        // do nothing
      }

      // enjoy next line
      ENTER_CHAR -> line++

      else -> {
        if (isDigit(char)) return scanNumber()
        if (isAlpha(char)) return scanIdentifier()

        throw LexError(line, char.toString())
      }
    }
  }

  private fun scanIdentifier() {
    while (isAlphaNumeric(peek()) && !isAtEnd) advance()

    addToken(RESERVED_KEYWORDS.getOrElse(source.substring(start, current)) {
      TokenType.Identifier
    })
  }

  private fun scanMultilineComments() {
    while (peek() != '*' && peekNext() != '/' && !isAtEnd) {
      if (peek() == ENTER_CHAR) line++

      advance()
    }

    if (isAtEnd) throw LexError(line, "Unfinished multiline comment")

    // close comment: */ here
    advance()
    advance()
  }

  // TODO: throw a language exception if double doesn't parse
  private fun scanNumber() {
    while (isDigit(peek()) && !isAtEnd) advance()

    if (peek() == '.' && isDigit(peekNext())) {
      advance()

      while (isDigit(peek()) && !isAtEnd) advance()

      addToken(TokenType.Double, source.substring(start, current).toDouble())
    } else addToken(TokenType.Int, source.substring(start, current).toInt())
  }

  private fun scanString() {
    while (peek() != '"' && !isAtEnd) {
      if (peek() == '\n') line++

      advance()
    }

    if (isAtEnd) throw LexError(line, "Unfinished string")

    // close string here
    advance()

    // append token
    // remove quotes from value to append in tokens
    val value = source.substring(start + 1, current - 1)
    addToken(TokenType.String, value)
  }

  private fun peekNext(): Char {
    if (current + 1 >= source.length) return '0'

    return source[current + 1]
  }

  private fun peek(): Char {
    if (isAtEnd) return '0'

    return source[current]
  }

  private fun advance(): Char {
    current++
    return source[current - 1]
  }

  private fun match(char: Char): Boolean {
    if (isAtEnd) return false
    if (source[current] != char) return false

    current++

    return true
  }

  private fun addToken(type: TokenType) {
    addToken(type, null)
  }

  private fun addToken(type: TokenType, literal: Any?) {
    tokens += Token(type, source.substring(start, current), literal, line)
  }
}

private fun isDigit(char: Char): Boolean = char in '0'..'9'
private fun isAlpha(char: Char): Boolean = char in 'a'..'z' || char in 'A'..'Z' || char == '_'
private fun isAlphaNumeric(char: Char): Boolean = isAlpha(char) || isDigit(char)
