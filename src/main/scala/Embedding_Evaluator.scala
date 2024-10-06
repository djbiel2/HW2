import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import java.io.File
import java.util.{List => JList, Collection => JCollection}
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Embedding_Evaluator {

  // Creating a logger
  private val logger = LoggerFactory.getLogger(Embedding_Evaluator.getClass)

  // Function to find words that are semantically close to each other
  def evaluate_embeddings(model_output_path: String, words_to_evaluate: List[String]): Unit = {
    logger.info("Starting evaluation of the embeddings")

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

      // Iterate over each word and find words that are semantically close to each other
      words_to_evaluate.foreach { word =>
        if (word2vec_model.hasWord(word)) {
          val close_words: JCollection[String] = word2vec_model.wordsNearest(word, 4)
          val close_words_scala = close_words.asScala.mkString(", ")

          // Log the result for each word
          logger.info(s"Word '$word' is semantically close to words $close_words_scala")
          println(s"Word '$word' is semantically close to words$close_words_scala")
        } else {
          logger.warn(s"Word '$word' not found")
        }
      }
    } catch {
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
