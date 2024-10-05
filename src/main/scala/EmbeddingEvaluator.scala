import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import java.io.File
import java.util.{List => JList, Collection => JCollection}
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object EmbeddingEvaluator {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(EmbeddingEvaluator.getClass)

  /**
   * Function to evaluate word embeddings by finding words that are semantically close to each word in the corpus.
   * @param modelPath The path to the pre-trained Word2Vec model.
   */
  def evaluateEmbeddings(modelPath: String, wordsToEvaluate: List[String]): Unit = {
    logger.info("Starting evaluation of embeddings.")

    try {
      // Load the model using WordVectorSerializer
      logger.info(s"Loading Word2Vec model from: $modelPath")
      val modelFile = new File(modelPath)

      if (!modelFile.exists()) {
        logger.error(s"Model file not found at path: $modelPath")
        return
      }

      val word2Vec: WordVectors = WordVectorSerializer.readWord2VecModel(modelFile)
      logger.info("Model successfully loaded.")

      // Iterate over each word in the provided list and find semantically close words
      wordsToEvaluate.foreach { word =>
        if (word2Vec.hasWord(word)) {
          logger.info(s"Finding words semantically close to: '$word'")
          val closeWords: JCollection[String] = word2Vec.wordsNearest(word, 4)
          val closeWordsScala = closeWords.asScala.mkString(", ")

          // Log the result
          logger.info(s"For the word '$word': semantically close: $closeWordsScala")
          println(s"For the word '$word': semantically close: $closeWordsScala")
        } else {
          logger.warn(s"Word '$word' not found in the vocabulary.")
        }
      }
    } catch {
      case e: Exception =>
        logger.error("An error occurred while evaluating embeddings.", e)
    }
  }

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()
    val modelPath = config.getString("model.path")
    val wordsToEvaluate = config.getStringList("wordsToEvaluate").asScala.toList

    // Call evaluate embeddings
    evaluateEmbeddings(modelPath, wordsToEvaluate)
  }
}
