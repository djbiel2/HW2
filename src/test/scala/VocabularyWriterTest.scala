import org.scalatest.funsuite.AnyFunSuite
import java.io.File

class VocabularyWriterTest extends AnyFunSuite {

  test("Generate vocabulary") {
    //need to add tests and the resources to it
    val shardDir = "test/Resources/shards"
    val outputFilePath = "test/Resources/vocabulary.csv"

    Vocabulary_Writer.generate_vocabulary_with_frequency(shardDir, outputFilePath)

    val outputFile = new File(outputFilePath)
    assert(outputFile.exists(), "Output file should have been created")

    val lines = scala.io.Source.fromFile(outputFile).getLines().toList
    assert(lines.nonEmpty, "VOcab should NOT be empty")
  }
}
