import org.scalatest.funsuite.AnyFunSuite
import java.io.File

class VocabularyWriterTest extends AnyFunSuite {

  test("generateVocabularyWithFrequency should create a vocabulary CSV with correct data") {
    val shardDir = "testResources/outputShards"
    val outputFilePath = "testResources/vocabulary.csv"

    VocabularyWriter.generateVocabularyWithFrequency(shardDir, outputFilePath)

    val outputFile = new File(outputFilePath)
    assert(outputFile.exists(), "The vocabulary output file should be created.")

    val lines = scala.io.Source.fromFile(outputFile).getLines().toList
    assert(lines.nonEmpty, "The vocabulary output file should not be empty.")
    assert(lines.head == "Word,Frequency", "The vocabulary output file should contain a header.")
  }
}
