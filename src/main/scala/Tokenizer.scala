import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.io.Source
import java.io.File
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Tokenizer {

//logger
  private val logger = LoggerFactory.getLogger(Tokenizer.getClass)


// Function to tokenize shards

  def tokenize_shard(shard_path: String): IntArrayList = {
    try {
      logger.info(s"Tokenizing shard -$shard_path")
      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)
      val shard_text = Source.fromFile(shard_path).mkString
      val encoded: IntArrayList = encoding.encode(shard_text)

    //check if tokens are empty
      if (encoded.isEmpty) {
        logger.warn(s"Tokens are empty $shard_path")
      } else {
        logger.info(s"Tokens completed $shard_path ")
      }

      encoded
    } catch {
      case e: Exception =>
        logger.error(s"Tokenizing error", e)
        new IntArrayList()
    }
  }

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()
    val shard_directory_path = config.getString("tokenizer.shard_directory")

    try {
      val shard_directory = new File(shard_directory_path)
      if (!shard_directory.exists() || !shard_directory.isDirectory) {
        logger.error(s"Shard directory not found, error")
        return
      }

      val shard_files = shard_directory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))
      if (shard_files.isEmpty) {
        logger.warn(s"No shard files found, error")
        return
      }

      // Iterate over all shards
      shard_files.foreach { shard_file =>
        logger.info(s"Processing shard- ${shard_file.getName}")
        val tokenized = tokenize_shard(shard_file.getAbsolutePath)
        if (!tokenized.isEmpty) {
          logger.debug(s"Encoded tokens for ${shard_file.getName}")
        }
      }
    } catch {
      case e: Exception =>
        logger.error("Processing error for shards for tokens", e)
    }
  }
}
