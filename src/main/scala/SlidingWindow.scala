import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.io.Source
import java.io.File
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object SlidingWindow {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(SlidingWindow.getClass)

  /**
   * Apply a sliding window on tokenized data to generate input-target pairs for training.
   * @param tokens Tokenized data represented as an array of integers.
   * @param windowSize The size of the sliding window.
   * @return A sequence of input-target pairs.
   */
  def applySlidingWindow(tokens: Array[Int], windowSize: Int): Seq[(Array[Int], Int)] = {
    tokens.sliding(windowSize + 1).collect {
      case window if window.length == windowSize + 1 =>
        val inputTokens = window.init
        val targetToken = window.last
        (inputTokens, targetToken)
    }.toSeq
  }

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()
    val shardDirectoryPath = config.getString("slidingWindow.shardDirectory")
    val windowSize = config.getInt("slidingWindow.windowSize")

    try {
      // Load shard files from the specified directory
      val shardDirectory = new File(shardDirectoryPath)
      if (!shardDirectory.exists() || !shardDirectory.isDirectory) {
        logger.error(s"Shard directory not found or is not a directory: $shardDirectoryPath")
        return
      }

      val shardFiles = shardDirectory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))
      if (shardFiles.isEmpty) {
        logger.warn(s"No shard files found in the directory: $shardDirectoryPath")
        return
      }

      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

      // Iterate over all shard files, tokenize them, and apply the sliding window
      shardFiles.foreach { shardFile =>
        logger.info(s"Processing shard: ${shardFile.getName}")
        val shardText = Source.fromFile(shardFile.getAbsolutePath).mkString
        val tokenized: IntArrayList = encoding.encode(shardText)
        val tokensArray: Array[Int] = tokenized.toArray

        // Apply the sliding window function to the tokenized data
        logger.info(s"Applying sliding window of size $windowSize to shard: ${shardFile.getName}")
        val slidingWindowData = applySlidingWindow(tokensArray, windowSize)

        // Log the input-target pairs for visualization
        slidingWindowData.foreach {
          case (inputTokens, targetToken) =>
            logger.debug(s"Input: ${inputTokens.mkString(", ")} -> Target: $targetToken")
        }

        logger.info(s"Completed processing shard: ${shardFile.getName}")
      }
    } catch {
      case e: Exception =>
        logger.error("An error occurred while processing the sliding window.", e)
    }
  }
}
