import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import scala.io.Source
import java.io.File
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.rdd.RDD
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
//load configurations
    val config = ConfigFactory.load()
    val shard_directory_path = config.getString("tokenizer.shard_directory")
    //initialize spark configurations

    val conf = new SparkConf().setAppName("Tokenizer").setMaster("local[*]")
    val sc = new JavaSparkContext(conf)
    //check if shard exists
    try {
      val shard_directory = new File(shard_directory_path)
      if (!shard_directory.exists() || !shard_directory.isDirectory) {
        logger.error("Error-shard directory not found ")
        return
      }

      // get shard files here
      val shard_files = shard_directory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))
      if (shard_files.isEmpty) {
        logger.warn("No shard files found in directory")
        return
      }

      // Parallelize the paths
      val shard_paths: RDD[String] = sc.parallelize(shard_files.map(_.getAbsolutePath).toList)
      val tokenized_shards_rdd: RDD[IntArrayList] = shard_paths.map(tokenize_shard)

      // collect results
      val tokenized_shards = tokenized_shards_rdd.collect()
      tokenized_shards.foreach { tokens =>
        logger.debug(s"Tokenized shard with ${tokens.size()} tokens")
      }

    } catch {
      case e: Exception =>
        logger.error("Error during tokenization step", e)
    } finally {
      // stop spark
      sc.stop()
    }
  }
}
