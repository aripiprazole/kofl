@file:Suppress("DuplicatedCode", "unused")

package com.lorenzoog.kofl.frontend.parser.lib

inline fun <reified T, R> combine(vararg functions: ParseFunc<T>, crossinline map: (List<T>) -> R): ParseFunc<R> =
  functions.toList().combineWith(map)

inline fun <reified T, R> List<ParseFunc<T>>.combineWith(crossinline map: (List<T>) -> R): ParseFunc<R> {
  require(isNotEmpty())

  return func@{ input ->
    val (_, values) = fold(input to emptyList<T>()) { (input_, values), f ->
      val result = f(input_).unwrapOr { return@func it.fix() }
      val first = values.firstOrNull() ?: result.data
      val tail = values.take(1)

      result.rest to listOf(first, *tail.toTypedArray())
    }

    ParseResult.Success(map(values))
  }
}

fun <T1, T2, R> combine(
  func1: ParseFunc<T1>,
  func2: ParseFunc<T2>,
  map: (T1, T2) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(map(result1.data, result2.data), result2.rest)
}

fun <T1, T2, T3, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  map: (T1, T2, T3) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(map(result1.data, result2.data, result3.data), result3.rest)
}

fun <T1, T2, T3, T4, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  map: (T1, T2, T3, T4) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(map(result1.data, result2.data, result3.data, result4.data), result4.rest)
}

fun <T1, T2, T3, T4, T5, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  map: (T1, T2, T3, T4, T5) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(map(result1.data, result2.data, result3.data, result4.data, result5.data), result5.rest)
}

fun <T1, T2, T3, T4, T5, T6, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  map: (T1, T2, T3, T4, T5, T6) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(result1.data, result2.data, result3.data, result4.data, result5.data, result6.data),
    result6.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  map: (T1, T2, T3, T4, T5, T6, T7) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(result1.data, result2.data, result3.data, result4.data, result5.data, result6.data, result7.data),
    result7.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  func8: ParseFunc<out T8>,
  map: (T1, T2, T3, T4, T5, T6, T7, T8) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }
  val result8 = func8(result7.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(
      result1.data,
      result2.data,
      result3.data,
      result4.data,
      result5.data,
      result6.data,
      result7.data,
      result8.data
    ),
    result8.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  func8: ParseFunc<out T8>,
  func9: ParseFunc<out T9>,
  map: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }
  val result8 = func8(result7.rest).unwrapOr { return@func it.fix() }
  val result9 = func9(result8.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(
      result1.data,
      result2.data,
      result3.data,
      result4.data,
      result5.data,
      result6.data,
      result7.data,
      result8.data,
      result9.data
    ),
    result9.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  func8: ParseFunc<out T8>,
  func9: ParseFunc<out T9>,
  func10: ParseFunc<out T10>,
  map: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }
  val result8 = func8(result7.rest).unwrapOr { return@func it.fix() }
  val result9 = func9(result8.rest).unwrapOr { return@func it.fix() }
  val result10 = func10(result9.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(
      result1.data,
      result2.data,
      result3.data,
      result4.data,
      result5.data,
      result6.data,
      result7.data,
      result8.data,
      result9.data,
      result10.data,
    ),
    result10.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  func8: ParseFunc<out T8>,
  func9: ParseFunc<out T9>,
  func10: ParseFunc<out T10>,
  func11: ParseFunc<out T11>,
  map: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }
  val result8 = func8(result7.rest).unwrapOr { return@func it.fix() }
  val result9 = func9(result8.rest).unwrapOr { return@func it.fix() }
  val result10 = func10(result9.rest).unwrapOr { return@func it.fix() }
  val result11 = func11(result10.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(
      result1.data,
      result2.data,
      result3.data,
      result4.data,
      result5.data,
      result6.data,
      result7.data,
      result8.data,
      result9.data,
      result10.data,
      result11.data
    ),
    result11.rest
  )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> combine(
  func1: ParseFunc<out T1>,
  func2: ParseFunc<out T2>,
  func3: ParseFunc<out T3>,
  func4: ParseFunc<out T4>,
  func5: ParseFunc<out T5>,
  func6: ParseFunc<out T6>,
  func7: ParseFunc<out T7>,
  func8: ParseFunc<out T8>,
  func9: ParseFunc<out T9>,
  func10: ParseFunc<out T10>,
  func11: ParseFunc<out T11>,
  func12: ParseFunc<out T12>,
  map: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) -> R
): ParseFunc<R> = func@{ input ->
  val result1 = func1(input).unwrapOr { return@func it.fix() }
  val result2 = func2(result1.rest).unwrapOr { return@func it.fix() }
  val result3 = func3(result2.rest).unwrapOr { return@func it.fix() }
  val result4 = func4(result3.rest).unwrapOr { return@func it.fix() }
  val result5 = func5(result4.rest).unwrapOr { return@func it.fix() }
  val result6 = func6(result5.rest).unwrapOr { return@func it.fix() }
  val result7 = func7(result6.rest).unwrapOr { return@func it.fix() }
  val result8 = func8(result7.rest).unwrapOr { return@func it.fix() }
  val result9 = func9(result8.rest).unwrapOr { return@func it.fix() }
  val result10 = func10(result9.rest).unwrapOr { return@func it.fix() }
  val result11 = func11(result10.rest).unwrapOr { return@func it.fix() }
  val result12 = func12(result11.rest).unwrapOr { return@func it.fix() }

  ParseResult.Success(
    map(
      result1.data,
      result2.data,
      result3.data,
      result4.data,
      result5.data,
      result6.data,
      result7.data,
      result8.data,
      result9.data,
      result10.data,
      result11.data,
      result12.data,
    ),
    result12.rest
  )
}
