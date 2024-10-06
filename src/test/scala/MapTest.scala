import org.scalatest.funsuite.AnyFunSuite
import org.apache.hadoop.io.{IntWritable, Text}
import scala.collection.mutable

class MapTest extends AnyFunSuite {

  test("Word_Count_Mapper should produce correct mappings") {
    val mapper = new Word_Count_Mapper()

    // Test map_line
    val result = mapper.map_line("hello professor")

    // Expected output
    val expectedOutput = Seq((new Text("hello"), new IntWritable(1)), (new Text("professor"), new IntWritable(1)))

    assert(result == expectedOutput)
  }
}
