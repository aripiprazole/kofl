class KoflStruct(stmt: Stmt.TypeDef.Struct) : KoflCallable.Common(stmt.fieldsDef.size) {
  private val fieldsDef = stmt.fieldsDef
  val name = stmt.name

  override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
    val fields = mutableMapOf<String, KoflValue>()

    arguments.forEachIndexed { i, argument ->
      fields[fieldsDef[i].lexeme] = argument.asKoflValue()
    }

    return KoflInstance(this, fields)
  }

  override fun toString(): String = "struct $name"
}

sealed class KoflCallable(val arity: Int) : KoflObject() {
  abstract class Common internal constructor(arity: Int) : KoflCallable(arity) {
    abstract operator fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject
    abstract override fun toString(): String
  }

  class Native(arity: Int, private val call: KoflFunction) : Common(arity) {
    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      return call(arguments, environment)
    }

    override fun toString(): String = "<native func>"
  }

  class AnonymousFunc(private val decl: Expr.AnonymousFunc, private val evaluator: Evaluator) :
    Common(decl.arguments.size) {

    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func (")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }

      append("): Any { <anonymous> }")
    }
  }

  class Func(private val decl: Expr.Func, private val evaluator: Evaluator) :
    Common(decl.arguments.size) {
    override fun invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func ${decl.name}(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }

      append("): Any { TODO }")
    }
  }

  class ExtensionFunc(private val decl: Expr.ExtensionFunc, private val evaluator: Evaluator) :
    KoflCallable(decl.arguments.size) {
    operator fun KoflObject.invoke(arguments: List<KoflObject>, environment: MutableEnvironment): KoflObject {
      val localEnvironment = MutableEnvironment(environment)

      localEnvironment.define(
        name = "this",
        value = asKoflValue()
      )

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asKoflValue())
      }

      return evaluator.eval(decl.body, localEnvironment).lastOrNull() ?: KoflUnit
    }

    override fun toString(): String = buildString {
      append("func ${decl.receiver} ${decl.name}(")

      if (decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it)
        }
      }

      append("): Any { TODO }")
    }
  }
}
