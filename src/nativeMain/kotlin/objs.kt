typealias KoflFunction = (List<Any>, MutableEnvironment) -> Unit

interface KoflObject

sealed class KoflCallable(val arity: Int) : KoflObject {
  abstract operator fun invoke(arguments: List<Any>, environment: MutableEnvironment): Any

  abstract override fun toString(): String

  class Native(arity: Int, private val call: KoflFunction) : KoflCallable(arity) {
    override fun invoke(arguments: List<Any>, environment: MutableEnvironment): Any {
      return call(arguments, environment)
    }

    override fun toString(): String = "<native func>"
  }

  class Func(private val decl: Expr.Func) : KoflCallable(decl.arguments.size) {
    override fun invoke(arguments: List<Any>, environment: MutableEnvironment): Any {
      val localEnvironment = MutableEnvironment(environment)

      arguments.forEachIndexed { i, argument ->
        localEnvironment.define(decl.arguments[i], argument.asValue())
      }

      return eval(decl.body)
    }

    override fun toString(): String = buildString {
      append("func ${decl.name.literal}(")

      if(decl.arguments.size > 1) {
        decl.arguments.forEach {
          append(", ").append(it.literal)
        }
      }

      append("): Any { TODO }")
    }
  }
}

