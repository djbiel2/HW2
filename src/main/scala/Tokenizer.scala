import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.io.Source
import java.io.File
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Tokenizer {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(Tokenizer.getClass)

  /**
   * Function to tokenize each shard using the specified encoding.
   * @param shardPath Path to the shard file to tokenize.
   * @return Encoded tokens as an IntArrayList.
   */
  def tokenizeShard(shardPath: String): IntArrayList = {
    try {
      logger.info(s"Tokenizing shard at path: $shardPath")
      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)
      val shardText = Source.fromFile(shardPath).mkString
      val encoded: IntArrayList = encoding.encode(shardText)

      // Check if the encoded tokens are empty
      if (encoded.isEmpty) {
        logger.warn(s"Tokenization produced an empty result for shard: $shardPath")
      } else {
        logger.info(s"Tokenization complete for shard: $shardPath with ${encoded.size()} tokens")
      }

      encoded
    } catch {
      case e: Exception =>
        logger.error(s"An error occurred while tokenizing shard: $shardPath", e)
        new IntArrayList()
    }
  }

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()
    val shardDirectoryPath = config.getString("tokenizer.shardDirectory")

    try {
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

      // Iterate over all shard files and tokenize them
      shardFiles.foreach { shardFile =>
        logger.info(s"Processing shard: ${shardFile.getName}")
        val tokenized = tokenizeShard(shardFile.getAbsolutePath)
        if (!tokenized.isEmpty) {
          logger.debug(s"Encoded Tokens for ${shardFile.getName}: ${tokenized.toArray.mkString(", ")}")
        }
      }
    } catch {
      case e: Exception =>
        logger.error("An error occurred while processing shard files for tokenization.", e)
    }
  }
}
