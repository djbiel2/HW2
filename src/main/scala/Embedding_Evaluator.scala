import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import java.io.File
import java.util.{List => JList, Collection => JCollection}
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import org.nd4j.linalg.api.ndarray.INDArray
import java.lang.management.ManagementFactory


object Embedding_Evaluator {

  // creating a logger
  private val logger = LoggerFactory.getLogger(Embedding_Evaluator.getClass)

  // Function to find words that are semantically close to each other
  def evaluate_embeddings(model_output_path: String, words_to_evaluate: List[String]): Unit = {
    logger.info("Starting embedding function")
    val start_time = System.currentTimeMillis()

    try {
      // Loading model
      logger.info(s"Loading model ")
      val model_file = new File(model_output_path)

      if (!model_file.exists()) {
        logger.error(s"Model not found ")
        return
      }

      val word2vec_model: WordVectors = WordVectorSerializer.readWord2VecModel(model_file)
      logger.info("Model loaded")

      val runtime = Runtime.getRuntime
      val operating_system = ManagementFactory.getOperatingSystemMXBean
      // Iterate over each word and find words that are semantically close to each other
      words_to_evaluate.foreach { word =>

        val wordstart_time = System.currentTimeMillis()

        if (word2vec_model.hasWord(word)) {
          val close_words: JCollection[String] = word2vec_model.wordsNearest(word, 4)
          val close_words_scala = close_words.asScala.mkString(", ")

          // Log the result for each word
          logger.info(s"Word '$word' is semantically close to words $close_words_scala")
          println(s"Word '$word' is semantically close to words$close_words_scala")
          //get the memory usage
          val memory_usage = (runtime.totalMemory - runtime.freeMemory)/(1024*1024)
          logger.info(s"Memory usage for word-'$word': $memory_usage MB")
          logger.info(s"System load for word-'$word': ${operating_system.getSystemLoadAverage}")


        } else {
          logger.warn(s"Word '$word' not found")
        }
        logger.info(s"Time taken to evaluate word '$word': ${System.currentTimeMillis()-wordstart_time} ms")
      }


      val total_time = System.currentTimeMillis() - start_time
      logger.info(s"Total time: $total_time ms")

    }
    catch {
      case e: Exception =>
        logger.error("Error embedding function", e)
    }
  }




  def main(args: Array[String]): Unit = {
    // Load configs
    val config = ConfigFactory.load()
    val model_output_path = config.getString("trainer.model_output_path")
    val words_to_evaluate = config.getStringList("words_to_evaluate").asScala.toList

    // Call embeddings evaluation
    evaluate_embeddings(model_output_path, words_to_evaluate)
  }
}
