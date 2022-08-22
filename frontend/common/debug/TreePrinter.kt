package me.devgabi.kofl.frontend.debug

import me.devgabi.kofl.frontend.ENTER_CHAR
import me.devgabi.kofl.frontend.Expr
import me.devgabi.kofl.frontend.Stmt
import kotlin.js.JsName
import kotlin.jvm.JvmName

// FIXME fix print random nulls in the root
fun Any.printAvlTree(
  buffer: StringBuilder = StringBuilder(),
  prefix: String = "",
  childrenPrefix: String = "",
  value: Any? = null
): String {
  buffer.append(prefix)
  buffer.append(value ?: this::class.simpleName ?: "")
  buffer.append(ENTER_CHAR)

  fun print(name: String = "", value: Any?): String {
    return when (value) {
      is Stmt, is Expr, is List<*> -> {
        value.printAvlTree(StringBuilder(), "$childrenPrefix├── $name: ", "$childrenPrefix│   ")
      }
      else -> {
        value
          ?.printAvlTree(
            StringBuilder(),
            "$childrenPrefix├── $name: ",
            "$childrenPrefix│   ",
            value
          )
          .toString()
      }
    }
  }

  return buffer.toString() + fields.entries.joinToString(separator = "") { (name, value) ->
    print(name, value)
  }
}

@JsName("printAvlTreeListExpr")
@JvmName("printAvlTreeListExpr")
fun List<Expr>.printAvlTree(
  buffer: StringBuilder,
  prefix: String = "",
  childrenPrefix: String = ""
): String {
  return joinToString { it.printAvlTree(buffer, prefix, childrenPrefix) }
}

@JsName("printAvlTreeListStmt")
@JvmName("printAvlTreeListStmt")
fun List<Stmt>.printAvlTree(
  buffer: StringBuilder,
  prefix: String = "",
  childrenPrefix: String = ""
): String {
  return joinToString { it.printAvlTree(buffer, prefix, childrenPrefix) }
}
