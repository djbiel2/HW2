import org.scalatest.funsuite.AnyFunSuite

class SlidingWindowTest extends AnyFunSuite {

  test("SlidingWindow should create correct input-output pairs") {
    val tokens = Array(1, 2, 3, 4, 5)
    val windowSize = 3

    val result = SlidingWindow.applySlidingWindow(tokens, windowSize).toList
    val expected = List(
      (Array(1, 2, 3), 4),
      (Array(2, 3, 4), 5)
    )
    assert(result == expected, "SlidingWindow should create the correct input-output pairs.")
  }

  test("SlidingWindow should return empty sequence for empty tokens array") {
    val tokens = Array.empty[Int]
    val windowSize = 3

    val result = SlidingWindow.applySlidingWindow(tokens, windowSize)
    assert(result.isEmpty, "SlidingWindow should return an empty sequence for an empty tokens array.")
  }
}
