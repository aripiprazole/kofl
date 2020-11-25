package com.lorenzoog.kofl.interpreter

class StackOverflowException : Error()
class StackUnderflowException : Error()
class StackOutOfBoundsException(stack: Stack<*>, index: Int) : Error("STACK OUT OF BOUNDS WHEN TRYING $index IN $stack")

interface Stack<T> {
  val size: Int
  val top: Int
  val isFull: Boolean
  val isEmpty: Boolean

  operator fun get(index: Int): T?

  fun push(item: T)
  fun pop(): T
  fun peek(): T
}

@Suppress("FunctionName")
fun <T> Stack(size: Int): Stack<T> {
  return StackImpl(size)
}

private class StackImpl<T>(override val size: Int) : Stack<T> {
  override val isFull get() = top >= size
  override val isEmpty get() = top <= 0
  override var top = 0

  @Suppress("UNCHECKED_CAST")
  private val items: Array<T?> = Array<Any?>(size) { null } as Array<T?>

  override operator fun get(index: Int): T? = try {
    items[index]
  } catch (ignored: Throwable) {
    null
  }

  override fun push(item: T) {
    if (isFull) throw StackOverflowException()

    items[top] = item
    top++
  }

  override fun pop(): T {
    if (isEmpty) throw StackUnderflowException()

    top--
    return items[top]!!
  }

  override fun peek(): T {
    return get(top - 1) ?: throw StackOutOfBoundsException(this, top -1)
  }

  override fun toString(): String = items.toList().toString()
}