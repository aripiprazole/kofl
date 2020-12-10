package com.lorenzoog.kofl.vm

import com.lorenzoog.kofl.frontend.Parser
import com.lorenzoog.kofl.frontend.Scanner
import com.lorenzoog.kofl.frontend.dump
import com.lorenzoog.kofl.interpreter.internal.create_heap
import com.lorenzoog.kofl.interpreter.internal.heap_alloc
import com.lorenzoog.kofl.interpreter.internal.heap_free
import com.lorenzoog.kofl.interpreter.internal.heap_t
import kotlinx.cinterop.*
import platform.posix.size_t

inline fun Heap(size: size_t): CPointer<heap_t>? {
  return create_heap(size.toInt())
}

inline fun <reified T : CVariable> CPointer<heap_t>?.use(f: (CPointer<T>) -> Unit) {
  alloc<T>()
    ?.also(f)
    ?.also(::free)
}

inline fun <reified T : CVariable> CPointer<heap_t>?.alloc(): CPointer<T>? {
  return heap_alloc(this, sizeOf<T>().toULong())?.reinterpret()
}

inline fun CPointer<heap_t>?.free(ptr: CPointer<*>?): Boolean {
  return heap_free(this, ptr)?.pointed?.value ?: false
}

fun main(): Unit = memScoped {
  val heap = Heap(1024u)

  heap.use<IntVar> { ptr ->
    ptr.pointed.value = 40

    println("PTR ${ptr.pointed}")
  }

  val compiler = Compiler()
  val code = """
    val x: String = "!";
    val y: String = x;
    "";
  """.trimIndent()
  val scanner = Scanner(code)
  val parser = Parser(scanner.scan(), repl = false)
  val stmts = parser.parse().also {
    dump(it)
  }

  println("COMPILED:")
  val chunk = compiler.compile(stmts).first().apply {
    disassemble("CODE")
  }
  println("==-==-==-==")

  val vm = KVM(this)
  vm.start()
  vm.interpret(arrayOf(chunk))
}