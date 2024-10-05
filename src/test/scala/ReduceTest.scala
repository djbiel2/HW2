import org.scalatest.funsuite.AnyFunSuite
import org.apache.hadoop.io.IntWritable
import scala.collection.JavaConverters._

class ReduceTest extends AnyFunSuite {

  test("WordCountReducer should correctly sum values") {
    val reducer = new WordCountReducer()

    // Test the reduceValues function directly
    val values = List(new IntWritable(1), new IntWritable(2), new IntWritable(3)).asJava
    val result = reducer.reduceValues(values)

    // Expected output
    val expectedSum = 6

    // Assert the sum
    assert(result == expectedSum, "The reducer should correctly sum the values.")
  }

  test("WordCountReducer should return 0 for empty values") {
    val reducer = new WordCountReducer()

    // Test the reduceValues function with an empty list
    val values = List.empty[IntWritable].asJava
    val result = reducer.reduceValues(values)

    // Expected output
    val expectedSum = 0

    // Assert the sum
    assert(result == expectedSum, "The reducer should return 0 for an empty list of values.")
  }
}
