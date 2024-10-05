import org.scalatest.funsuite.AnyFunSuite
import org.apache.hadoop.io.{IntWritable, Text}
import scala.collection.mutable

class MapTest extends AnyFunSuite {

  test("WordCountMapper should produce correct key-value pairs") {
    val mapper = new WordCountMapper()

    // Test the mapLine function directly
    val result = mapper.mapLine("hello world")

    // Expected output
    val expectedOutput = Seq((new Text("hello"), new IntWritable(1)), (new Text("world"), new IntWritable(1)))

    // Assert each output
    assert(result == expectedOutput)
  }
}
