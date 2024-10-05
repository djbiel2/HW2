import org.scalatest.funsuite.AnyFunSuite
import java.io.File

class CorpusSharderTest extends AnyFunSuite {

  test("splitCorpusIntoShards should create the expected number of shards") {
    val inputPath = "testResources/testDataset.txt"
    val shardSize = 50
    val outputDir = "testResources/outputShards"

    // Clean up any pre-existing shards
    val outputDirectory = new File(outputDir)
    outputDirectory.listFiles().foreach(_.delete())

    CorpusSharder.splitCorpusIntoShards(inputPath, shardSize, outputDir)

    val shards = outputDirectory.listFiles().filter(_.isFile)
    assert(shards.nonEmpty, "Shards should be created.")
    assert(shards.length == 2, "Two shards should be created for the given test dataset.")
  }

  test("splitCorpusIntoShards should handle empty input file gracefully") {
    val inputPath = "testResources/emptyDataset.txt"
    val shardSize = 50
    val outputDir = "testResources/outputShards"

    CorpusSharder.splitCorpusIntoShards(inputPath, shardSize, outputDir)

    val shards = new File(outputDir).listFiles().filter(_.isFile)
    assert(shards.isEmpty, "No shards should be created for an empty dataset.")
  }
}
