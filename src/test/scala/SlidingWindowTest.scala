import org.scalatest.funsuite.AnyFunSuite

class SlidingWindowTest extends AnyFunSuite {

  test("Sliding_Window for pairs test") {
    val tokens = Array(1, 2, 3, 4, 5)
    val windowSize = 3

    val result = Sliding_Window.apply_sliding_window(tokens, windowSize).toList
    val expected = List(
      (Array(1, 2, 3), 4),
      (Array(2, 3, 4), 5)
    )
    assert(result == expected, "Sliding_Window for pairs test")
  }

  test("Empty sequence") {
    val tokens = Array.empty[Int]
    val windowSize = 3

    val result = Sliding_Window.apply_sliding_window(tokens, windowSize)
    assert(result.isEmpty, "Empty sequence")
  }
}
