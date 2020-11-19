class Parser(private val tokens: List<Token>) {
  private val isAtEnd get() = peek().type == TokenType.Eof

  private var current = 0

  fun parse(): Collection<Stmt> {
    val stmts = mutableListOf<Stmt>()

    while (!isAtEnd) {
      stmts += declaration() ?: continue
    }

    return stmts.toSet()
  }

  private fun synchronize(): Boolean {
    advance()

    while (!isAtEnd) {
      if (previous().type == TokenType.Semicolon) return true

      when (peek().type) {
        TokenType.Struct, TokenType.Func,
        TokenType.Val, TokenType.If, TokenType.Else,
        TokenType.Return, TokenType.Var -> return true
        else -> {
          // do nothing
        }
      }

      advance()
    }

    return false
  }

  // stmts
  private fun declaration(): Stmt? = try {
    when {
      match(TokenType.Val) -> valDeclaration()
      match(TokenType.Var) -> varDeclaration()

      else -> statement()
    }
  } catch (error: ParseError) {
    // panic mode
    if (!synchronize())
      error.report()

    null
  }

  private fun valDeclaration(): Stmt {
    val name = consume(TokenType.Identifier)
      ?: throw ParseError(peek(), "expecting a declaration name")

    if (!match(TokenType.Equal))
      throw ParseError(peek(), "expecting a declaration initializer")

    val initializer = expression()

    consume(TokenType.Semicolon)
      ?: throw ParseError(peek(), "expecting a semicolon after a declaration")

    return Stmt.ValDecl(name, initializer)
  }

  private fun varDeclaration(): Stmt {
    val name = consume(TokenType.Identifier)
      ?: throw ParseError(peek(), "expecting a declaration name")

    if (!match(TokenType.Equal))
      throw ParseError(peek(), "expecting a declaration initializer")

    val initializer = expression()

    consume(TokenType.Semicolon)
      ?: throw ParseError(peek(), "expecting a semicolon after a declaration")

    return Stmt.VarDecl(name, initializer)
  }

  private fun statement(): Stmt {
    if (match(TokenType.Print)) return printStatement()

    return exprStatement()
  }

  private fun printStatement(): Stmt {
    val expr = expression()

    consume(TokenType.Semicolon) ?: throw ParseError(peek(), "Missing end of line")

    return Stmt.PrintStmt(expr)
  }

  private fun exprStatement(): Stmt {
    val expr = expression()

    consume(TokenType.Semicolon) ?: throw ParseError(peek(), "Missing end of line")

    return Stmt.ExprStmt(expr)
  }

  // expressions
  private fun expression(): Expr {
    val expr = equality()

    if (match(TokenType.Equal)) {
      val equals = previous()
      val value = assignment()

      if (expr is Expr.Var) {
        val (name) = expr

        return Expr.Assign(name, value)
      }

      // we report the error but don't throw
      // to enter in panic mode and synchronize
      ParseError(equals, "Invalid right-associative assignment")
        .report()
    }

    return expr
  }

  private fun assignment(): Expr {
    return equality()
  }

  private fun equality(): Expr {
    var expr = comparison()

    while (match(TokenType.BangEqual, TokenType.EqualEqual)) {
      val op = previous()
      val right = comparison()

      expr = Expr.Binary(expr, op, right)
    }

    return expr
  }

  private fun comparison(): Expr {
    var expr = term()

    while (match(TokenType.GreaterEqual, TokenType.Greater, TokenType.Less, TokenType.LessEqual)) {
      val op = previous()
      val right = term()

      expr = Expr.Binary(expr, op, right)
    }

    return expr
  }

  private fun term(): Expr {
    var expr = factor()

    while (match(TokenType.Minus, TokenType.Plus, TokenType.Bang)) {
      val op = previous()
      val right = factor()

      expr = Expr.Binary(expr, op, right)
    }

    return expr
  }

  private fun factor(): Expr {
    var expr = unary()

    while (match(TokenType.Slash, TokenType.Star)) {
      val op = previous()
      val right = unary()

      expr = Expr.Binary(expr, op, right)
    }

    return expr
  }

  private fun unary(): Expr {
    if (match(TokenType.Bang, TokenType.Minus, TokenType.Plus)) {
      val op = previous()
      val right = unary()

      return Expr.Unary(op, right)
    }

    return primary()
  }

  private fun primary(): Expr = when {
    match(TokenType.False) -> Expr.Literal(false)
    match(TokenType.True) -> Expr.Literal(true)

    match(TokenType.Number, TokenType.String) -> Expr.Literal(previous().let {
      it.literal ?: ""
    }) // TODO: fixme

    match(TokenType.LeftParen) -> Expr.Grouping(expression().also {
      consume(TokenType.RightParen) ?: throw ParseError(peek(), "Unfinished grouping")
    })

    match(TokenType.Identifier) -> Expr.Var(previous())

    else -> throw ParseError(peek())
  }

  // utils
  private fun consume(type: TokenType): Token? {
    if (check(type)) return advance()

    return null
  }

  private fun match(vararg types: TokenType): Boolean {
    for (type in types) {
      if (check(type)) {
        advance()
        return true
      }
    }

    return false
  }

  private fun check(type: TokenType): Boolean {
    if (isAtEnd) return false

    return peek().type == type
  }

  private fun advance(): Token {
    if (!isAtEnd) current++

    return previous()
  }

  private fun peek(): Token {
    return tokens[current]
  }

  private fun previous(): Token {
    return tokens[current - 1]
  }
}
