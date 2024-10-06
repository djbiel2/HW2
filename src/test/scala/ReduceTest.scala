import org.scalatest.funsuite.AnyFunSuite
import org.apache.hadoop.io.IntWritable
import scala.collection.JavaConverters._

class ReduceTest extends AnyFunSuite {

  test("Word_Count_Reducer should sum values") {
    val reducer = new Word_Count_Reducer()

    // test reduce_values
    val values = List(new IntWritable(1), new IntWritable(2), new IntWritable(3)).asJava
    val result = reducer.reduce_values(values)

    // Expected output
    val expectedSum = 6
    assert(result == expectedSum, "Reducer should correctly reduce.")
  }

  test("Word_Count_Reducer must return 0 for empty list") {
    val reducer = new Word_Count_Reducer()

    // test with empty list
    val values = List.empty[IntWritable].asJava
    val result = reducer.reduce_values(values)

    // Expected output
    val expectedSum = 0


    assert(result == expectedSum, "The reducer should return 0")
  }
}
