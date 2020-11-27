package com.lorenzoog.kofl.frontend

private const val MAX_ARGS = 32  // the limit is really 31 'cause the this is passed as a arg
private const val MAX_ARGS_ERROR_MESSAGE = "can't have more than $MAX_ARGS arguments in a function"
private const val INVALID_RIGHT_ASSOCIATIVE_ERROR_MESSAGE = "invalid right-associative assignment"

class Parser(private val tokens: List<Token>, private val repl: Boolean = false) {
  private val isAtEnd get() = peek().type == TokenType.Eof
  private var current = 0

  fun parse(): List<Stmt> {
    val stmts = mutableListOf<Stmt>()

    while (!isAtEnd) {
      stmts += declaration() ?: continue
    }

    return stmts
  }

  private fun synchronize() {
    advance()

    while (!isAtEnd) {
      when (peek().type) { // TODO: report useless char
        TokenType.Class, TokenType.Func,
        TokenType.Val, TokenType.If, TokenType.Else,
        TokenType.Return, TokenType.Var,
        TokenType.Semicolon -> return
        else -> {
          // do nothing
        }
      }

      advance()
    }
  }

  // stmts
  enum class ScopeType { Global, Func }

  private fun declaration(): Stmt? = try {
    when {
      match(TokenType.Val) -> valDeclaration()
      match(TokenType.Var) -> varDeclaration()
      match(TokenType.Type) -> classDeclaration()
      match(TokenType.LeftBrace) -> Stmt.Block(block(), line())
      match(TokenType.External) -> attributedStatement(mutableListOf(TokenType.External))
      match(TokenType.Func) -> Stmt.ExprStmt(funcExpr(FuncType.Func), line())

      else -> if (repl) statement() else throw error(expecting("declaration"))
    }
  } catch (exception: ParseException) {
    // panic mode
    synchronize()
    exception.report()

    null
  }

  private fun block(scopeType: ScopeType = ScopeType.Global): MutableList<Stmt> {
    val stmts = mutableListOf<Stmt>()

    while (!check(TokenType.RightBrace) && !isAtEnd) {
      stmts += statement(scopeType)
    }

    consume(TokenType.RightBrace) ?: throw error(expecting(end("block")))

    return stmts
  }

  private fun initializer(): Expr {
    if (!match(TokenType.Equal)) throw error(expecting("initializer"))

    val initializer = expression()

    consume(TokenType.Semicolon) ?: throw error(expecting(TokenType.Semicolon))

    return initializer
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun classDeclaration(): Stmt {
    consume(TokenType.Class) ?: throw error(expecting(TokenType.Class))

    val name = consume(TokenType.Identifier) ?: throw error(expecting("struct name"))
    val parameters: Map<Token, Token> = when {
      match(TokenType.LeftParen) -> parameters()
      else -> mapOf()
    }

    consume(TokenType.Semicolon) ?: throw error(expecting(TokenType.Semicolon))

    return Stmt.Type.Class(name, parameters, line())
  }

  private fun valDeclaration(): Stmt {
    val name = consume(TokenType.Identifier)
      ?: throw error(expecting("declaration name"))

    return Stmt.ValDecl(name, typeNotationOrNull(), initializer(), line())
  }

  private fun varDeclaration(): Stmt {
    val name = consume(TokenType.Identifier)
      ?: throw error(expecting("declaration name"))

    return Stmt.VarDecl(name, typeNotationOrNull(), initializer(), line())
  }

  private fun statement(scopeType: ScopeType = ScopeType.Global): Stmt {
    return when {
      match(TokenType.Return) -> when (scopeType) {
        ScopeType.Global -> throw error(notExpecting(TokenType.Return), token = previous())
        ScopeType.Func -> returnStatement()
      }
      match(TokenType.Val) -> valDeclaration()
      match(TokenType.Var) -> varDeclaration()
      match(TokenType.Type) -> classDeclaration()
      match(TokenType.While) -> whileStatement()
      match(TokenType.LeftBrace) -> Stmt.Block(block(), line())
      match(TokenType.If) -> Stmt.ExprStmt(ifExpr(IfType.If), line())
      match(TokenType.External) -> attributedStatement(mutableListOf(TokenType.External))
      match(TokenType.Func) -> Stmt.ExprStmt(funcExpr(FuncType.Func), line())

      else -> exprStatement()
    }
  }

  private fun attributedStatement(attributes: MutableList<TokenType>): Stmt {
    while (!check(TokenType.Func) && !check(TokenType.Class) && !check(TokenType.Identifier)) {
      attributes.add(advance().type)
    }

    return when {
      match(TokenType.Func) -> funcExpr(FuncType.Func, attributes).let {
        Stmt.ExprStmt(it, it.line)
      }
      match(TokenType.Class, TokenType.Identifier) -> classDeclaration()
      else -> throw error(expecting("declaration"))
    }
  }

  private fun returnStatement(): Stmt {
    val expression = if (!check(TokenType.Semicolon)) {
      expression()
    } else Expr.Literal(Unit, line()) // returns unit if hasn't value

    consume(TokenType.Semicolon) ?: throw error(expecting(TokenType.Semicolon))

    return Stmt.ReturnStmt(expression, line())
  }

  private fun whileStatement(): Stmt {
    val condition = expression()

    if (!match(TokenType.LeftBrace))
      throw error(expecting(start("while body")))

    return Stmt.WhileStmt(condition, block(), line())
  }

  private fun exprStatement(): Stmt {
    val expr = expression()

    consume(TokenType.Semicolon) ?: throw error(expecting(TokenType.Semicolon))

    return Stmt.ExprStmt(expr, line())
  }

  // expressions
  @OptIn(ExperimentalStdlibApi::class)
  private fun expression(): Expr {
    if (match(TokenType.If)) return ifExpr(IfType.Anonymous)
    if (match(TokenType.Func)) return funcExpr(FuncType.Anonymous)

    val expr = or()

    if (match(TokenType.Equal)) {
      val equals = previous()
      val value = assignment()

      if (expr is Expr.Var) {
        val (name, _) = expr

        return Expr.Assign(name, value, line())
      } else if (expr is Expr.Get) {
        return Expr.Set(expr.receiver, expr.name, value, line())
      }

      // we report the error but don't throw
      // to enter in panic mode and synchronize
      error(INVALID_RIGHT_ASSOCIATIVE_ERROR_MESSAGE, token = equals).report()
    }

    return expr
  }

  private enum class IfType { Anonymous, If }

  private fun ifExpr(type: IfType): Expr {
    val condition = expression()

    if (!match(TokenType.LeftBrace))
      throw error(expecting(start("if body")))

    val mainBranch = block()
    val elseBranch = if (match(TokenType.Else))
      if (match(TokenType.LeftBrace))
        block()
      else throw error(expecting(start("else body")))
    else null

    if (type == IfType.Anonymous && elseBranch == null) {
      throw error(expecting("else body on local if"))
    }

    return Expr.IfExpr(condition, mainBranch, elseBranch, line())
  }

  private enum class FuncType { Anonymous, Func }

  @OptIn(ExperimentalStdlibApi::class)
  private fun funcExpr(type: FuncType, modifiers: List<TokenType> = emptyList()): Expr {
    fun Token?.orThrow() = this ?: throw error(expecting("function's name"))

    val name = consume(TokenType.Identifier)

    if (match(TokenType.Identifier)) {
      return extensionFuncExpr(name.orThrow(), type, modifiers)
    } else {
      consume(TokenType.LeftParen) ?: throw error(expecting(start("arguments")))
    }

    val parameters = parameters()
    val returnType = typeNotationOrNull()
    val body = if (TokenType.External !in modifiers) funcBody(type) else {
      return Expr.NativeFunc(name.orThrow(), parameters, returnType, line())
        .also {
          requireSemicolon()
        }
    }

    return when (type) {
      FuncType.Anonymous -> Expr.AnonymousFunc(parameters, body, returnType, line())
      FuncType.Func -> Expr.CommonFunc(name.orThrow(), parameters, body, returnType, line())
    }
  }

  private fun extensionFuncExpr(receiver: Token, type: FuncType, modifiers: List<TokenType> = emptyList()): Expr {
    val name = previous()

    consume(TokenType.LeftParen) ?: throw error(expecting(start("arguments")))

    val parameters = parameters()
    val returnType = typeNotationOrNull()
    val body = if (TokenType.External !in modifiers) funcBody(type) else {
      return Expr.NativeFunc(name, parameters, returnType, line()).also {
        requireSemicolon()
      }
    }

    return Expr.ExtensionFunc(receiver, name, parameters, body, returnType, line())
  }

  private fun funcBody(type: FuncType): List<Stmt> {
    return when {
      consume(TokenType.LeftBrace) != null -> block(ScopeType.Func)
      consume(TokenType.Equal) != null -> listOf(Stmt.ReturnStmt(expression(), line())).also {
        if (type == FuncType.Func) requireSemicolon()
      }
      else -> throw error(expecting(start("function's body")))
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun parameters(): Map<Token, Token> {
    val parameters = buildMap<Token, Token> {
      if (!check(TokenType.RightParen))
        do {
          if (size >= MAX_ARGS) {
            error(MAX_ARGS_ERROR_MESSAGE).report()
          } else {
            val name = consume(TokenType.Identifier) ?: throw error(expecting("parameter's name"))
            val notation = typeNotation()

            this[name] = notation
          }
        } while (match(TokenType.Comma))
    }

    consume(TokenType.RightParen) ?: throw error(expecting(end("arguments")))

    return parameters
  }

  private fun assignment(): Expr {
    return or()
  }

  private fun or(): Expr {
    var expr = and()

    while (match(TokenType.Or)) {
      val op = previous()
      val right = equality()

      expr = Expr.Logical(expr, op, right, line())
    }

    return expr
  }

  private fun and(): Expr {
    var expr = equality()

    while (match(TokenType.And)) {
      val op = previous()
      val right = equality()

      expr = Expr.Logical(expr, op, right, line())
    }

    return expr
  }

  private fun equality(): Expr {
    var expr = comparison()

    while (match(TokenType.BangEqual, TokenType.EqualEqual)) {
      val op = previous()
      val right = comparison()

      expr = Expr.Binary(expr, op, right, line())
    }

    return expr
  }

  private fun comparison(): Expr {
    var expr = term()

    while (match(TokenType.GreaterEqual, TokenType.Greater, TokenType.Less, TokenType.LessEqual)) {
      val op = previous()
      val right = term()

      expr = Expr.Binary(expr, op, right, line())
    }

    return expr
  }

  private fun term(): Expr {
    var expr = factor()

    while (match(TokenType.Minus, TokenType.Plus, TokenType.Bang)) {
      val op = previous()
      val right = factor()

      expr = Expr.Binary(expr, op, right, line())
    }

    return expr
  }

  private fun factor(): Expr {
    var expr = unary()

    while (match(TokenType.Slash, TokenType.Star)) {
      val op = previous()
      val right = unary()

      expr = Expr.Binary(expr, op, right, line())
    }

    return expr
  }

  private fun unary(): Expr {
    if (match(TokenType.Bang, TokenType.Minus, TokenType.Plus)) {
      val op = previous()
      val right = unary()

      return Expr.Unary(op, right, line())
    }

    return call()
  }

  private fun call(): Expr {
    var expr = primary()

    while (true) expr = when {
      match(TokenType.LeftParen) -> finishCall(expr)
      match(TokenType.Dot) -> {
        val name = consume(TokenType.Identifier) ?: throw error(expecting("identifier after ${TokenType.Dot}"))

        Expr.Get(expr, name, line())
      }
      else -> break
    }

    return expr
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun arguments(): Map<Token?, Expr> {
    val arguments = buildMap<Token?, Expr> {
      if (!check(TokenType.RightParen)) {
        do {
          if (size >= MAX_ARGS) {
            error(MAX_ARGS_ERROR_MESSAGE).report()
          } else {
            val expr = expression()
            val colon = consume(TokenType.Colon)

            if (colon != null) {
              if (expr is Expr.Var) {
                this[expr.name] = expression()
              } else throw error(expecting("parameter's name"))
            } else this[null] = expr
          }
        } while (match(TokenType.Comma))
      }
    }

    consume(TokenType.RightParen) ?: throw error(expecting(TokenType.RightParen))

    return arguments
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun finishCall(callee: Expr): Expr {
    return Expr.Call(callee, arguments(), line())
  }

  private fun primary(): Expr = when {
    match(TokenType.False) -> Expr.Literal(false, line())
    match(TokenType.True) -> Expr.Literal(true, line())

    match(TokenType.This) -> Expr.ThisExpr(previous(), line())

    match(TokenType.Double, TokenType.String, TokenType.Int) -> Expr.Literal(previous().let {
      it.literal ?: ""
    }, line()) // TODO: fixme

    match(TokenType.LeftParen) -> Expr.Grouping(expression().also {
      consume(TokenType.RightParen) ?: throw error(expecting(TokenType.RightParen))
    }, line())

    match(TokenType.Identifier) -> Expr.Var(previous(), line())

    else -> throw error()
  }

  private fun typeNotation(): Token =
    typeNotationOrNull() ?: throw error(expecting("type notation"), token = previous())

  private fun typeNotationOrNull(): Token? {
    consume(TokenType.Colon) ?: return null

    return consume(TokenType.Identifier)
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

  private inline fun line(): Int {
    return previous().line
  }

  private inline fun peek(): Token {
    return tokens[current]
  }

  private inline fun previous(): Token {
    return tokens[current - 1]
  }

  private inline fun requireSemicolon(): Token = consume(TokenType.Semicolon)
    ?: throw error(expecting(TokenType.Semicolon))

  private inline fun error(message: String = "", token: Token = peek()) = ParseException(token, message)

  private inline fun expecting(type: Any) = "expecting $type"
  private inline fun notExpecting(type: Any) = "not expecting $type"
  private inline fun start(type: Any) = "start of $type"
  private inline fun end(type: Any) = "end of $type"
}

