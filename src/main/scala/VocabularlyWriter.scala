import java.io.{File, PrintWriter}
import scala.io.Source
import scala.collection.mutable
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object VocabularyWriter {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(VocabularyWriter.getClass)

  /**
   * Generate vocabulary with word frequency from the shard files and save it to the output file.
   * @param shardDir The directory containing shard files to process.
   * @param outputFilePath The path to save the vocabulary CSV.
   */
  def generateVocabularyWithFrequency(shardDir: String, outputFilePath: String): Unit = {
    try {
      // Create a mutable map to store word frequency
      val wordFrequency = mutable.Map[String, Int]().withDefaultValue(0)

      // Access the directory with the shards
      val shardDirectory = new File(shardDir)
      if (!shardDirectory.exists() || !shardDirectory.isDirectory) {
        logger.error(s"Shard directory not found or is not a directory: $shardDir")
        return
      }

      val shardFiles = shardDirectory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))
      if (shardFiles.isEmpty) {
        logger.warn(s"No shard files found in the directory: $shardDir")
        return
      }

      logger.info(s"Processing shard files from directory: $shardDir")

      // Iterate through each shard and count word frequencies
      shardFiles.foreach { shardFile =>
        logger.info(s"Processing shard: ${shardFile.getName}")
        val source = Source.fromFile(shardFile)
        source.getLines().foreach { line =>
          val words = line.split("\\s+").map(_.replaceAll("[^a-zA-Z]", "").toLowerCase).filter(_.nonEmpty)

          words.foreach { word =>
            wordFrequency(word) += 1
          }
        }
        source.close()
      }

      // Write the vocabulary and frequency to the output file
      logger.info(s"Writing vocabulary with frequency to output file: $outputFilePath")
      val writer = new PrintWriter(new File(outputFilePath))
      writer.println("Word,Frequency")
      wordFrequency.foreach {
        case (word, frequency) =>
          writer.println(s"$word,$frequency")
      }
      writer.close()
      logger.info("Vocabulary generation completed successfully.")
    } catch {
      case e: Exception =>
        logger.error("An error occurred while generating the vocabulary with frequency.", e)
    }
  }

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()
    val shardDir = config.getString("vocabularyWriter.shardDir")
    val outputFilePath = config.getString("vocabularyWriter.outputFilePath")

    // Call generate vocabulary with frequency
    generateVocabularyWithFrequency(shardDir, outputFilePath)
  }
}
