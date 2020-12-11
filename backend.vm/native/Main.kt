package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.interpreter.internal.*
import kotlinx.cinterop.*

inline fun <reified T : CVariable> CPointer<T>.forEach(f: (T) -> Unit) {
  for (index in 0..(sizeOf<T>() / sizeOf<T>())) {
    f(get(index))
  }
}

fun CPointer<mem_info_t>?.render(): String = buildString {
  if (this@render == null) return "null"

  append("[")
  this@render.forEach { memInfo ->
    append("mem_info_t(")
    append("is_free=${memInfo.is_free}, ")
    append("next=${memInfo.next.render()}, ")
    append("size=${memInfo.size}, ")
    append("), ")
  }
  append("]")
}

fun CPointer<heap_t>?.render(): String = buildString {
  if (this@render == null) return "null"

  append("[")
  this@render.forEach { heap ->
    append("heap_t(")
    append("end=${heap.end}, ")
    append("root=${heap.root.render()}, ")
    append("), ")
  }
  append("]")
}

fun main(): Unit = memScoped {
  val heap = create_heap(512_000)

  val pointer = heap_alloc(heap, sizeOf<IntVar>().toULong())?.reinterpret<IntVar>()?.apply {
    pointed.value = 34
  }

  println("HEAP=${heap.render()}")
  println("INTVAR=${pointer?.pointed?.value}")

  heap_free(heap, pointer)
//
//  val compiler = Compiler()
//  val code = """
//    val x: String = "!";
//    val y: String = x;
//    "";
//  """.trimIndent()
//  val scanner = Scanner(code)
//  val parser = Parser(scanner.scan(), repl = false)
//  val stmts = parser.parse().also {
//    dump(it)
//  }
//
//  println("COMPILED:")
//  val chunk = compiler.compile(stmts).first().apply {
//    disassemble("CODE")
//  }
//  println("==-==-==-==")
//
//  val vm = KVM(this)
//  vm.start()
//  vm.interpret(arrayOf(chunk))
}