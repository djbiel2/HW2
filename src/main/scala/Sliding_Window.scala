import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.io.Source
import java.io.File
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Sliding_Window {

  //logger
  private val logger = LoggerFactory.getLogger(Sliding_Window.getClass)


  //Sliding window function on the tokenized data

  def apply_sliding_window(tokens_array: Array[Int], window_size: Int): Seq[(Array[Int], Int)] = {
    tokens_array.sliding(window_size + 1).collect {
      case window if window.length == window_size + 1 =>
        val input_tokens = window.init
        val target_token = window.last
        (input_tokens, target_token)
    }.toSeq
  }

  def main(args: Array[String]): Unit = {
    //load configs
    val config = ConfigFactory.load()
    val shard_directory_path = config.getString("sliding_window.shard_directory")
    val window_size = config.getInt("sliding_window.window_size")

    try {
      // load shards
      val shard_directory = new File(shard_directory_path)
      if (!shard_directory.exists() || !shard_directory.isDirectory) {
        logger.error(s"Shards not found, error")
        return
      }

      val shard_files = shard_directory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))
      if (shard_files.isEmpty) {
        logger.warn(s"Shards not found")
        return
      }

      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

      // Iterate over all shard files, tokenize them, and apply the sliding window
      shard_files.foreach { shard_file =>
        logger.info(s"Shard-${shard_file.getName}")
        val shard_text = Source.fromFile(shard_file.getAbsolutePath).mkString
        val tokenized: IntArrayList = encoding.encode(shard_text)
        val tokens_array: Array[Int] = tokenized.toArray

        // Apply the sliding window function to the tokenized data
        logger.info(s"Window of size $window_size applied to shard- ${shard_file.getName}")
        val sliding_window_data = apply_sliding_window(tokens_array, window_size)

        // Log the pairs
        sliding_window_data.foreach {
          case (input_tokens, target_token) =>
            logger.debug(s"Input token ${input_tokens.mkString(", ")} -target token- $target_token")
        }

        logger.info(s"Finished processing shard-${shard_file.getName}")
      }
    } catch {
      case e: Exception =>
        logger.error("Error- sliding window", e)
    }
  }
}
