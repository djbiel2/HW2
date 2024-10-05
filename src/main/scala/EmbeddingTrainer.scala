import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator
import org.deeplearning4j.text.sentenceiterator.SentenceIterator
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import java.io.File

object EmbeddingTrainer {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(EmbeddingTrainer.getClass)

  /**
   * Train embeddings using the specified corpus directory and save the model.
   * @param corpusDir The directory containing the text corpus to train on.
   * @param modelOutputPath The path to save the trained model.
   * @param minWordFrequency Minimum word frequency for words to be included.
   * @param iterations Number of iterations to train the model.
   * @param layerSize Size of the word vectors.
   * @param windowSize Context window size for training.
   */
  def trainEmbeddings(corpusDir: String, modelOutputPath: String, minWordFrequency: Int, iterations: Int, layerSize: Int, windowSize: Int): Unit = {
    try {
      // Create a SentenceIterator to read the text from the shards
      logger.info(s"Loading text data from: $corpusDir")
      val corpusDirectory = new File(corpusDir)
      val sentenceIterator: SentenceIterator = new FileSentenceIterator(corpusDirectory)

      // Use DefaultTokenizerFactory to tokenize the sentences
      logger.info("Initializing the tokenizer.")
      val tokenizerFactory = new DefaultTokenizerFactory()

      // Create and configure the Word2Vec model
      logger.info(s"Configuring Word2Vec model with parameters: minWordFrequency=$minWordFrequency, iterations=$iterations, layerSize=$layerSize, windowSize=$windowSize")
      val word2Vec = new Word2Vec.Builder()
        .minWordFrequency(minWordFrequency)
        .iterations(iterations)
        .layerSize(layerSize)
        .windowSize(windowSize)
        .iterate(sentenceIterator)
        .tokenizerFactory(tokenizerFactory)
        .build()

      // Train the Word2Vec model
      logger.info("Starting training of Word2Vec model.")
      word2Vec.fit()
      logger.info("Training completed successfully.")

      // Save the trained model using WordVectorSerializer
      logger.info(s"Saving trained model to: $modelOutputPath")
      WordVectorSerializer.writeWord2VecModel(word2Vec, new File(modelOutputPath))
      logger.info("Model saved successfully.")
    } catch {
      case e: Exception =>
        logger.error("An error occurred during training.", e)
    }
  }

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()
    val corpusDir = config.getString("trainer.corpusDir")
    val modelOutputPath = config.getString("trainer.modelOutputPath")
    val minWordFrequency = config.getInt("trainer.minWordFrequency")
    val iterations = config.getInt("trainer.iterations")
    val layerSize = config.getInt("trainer.layerSize")
    val windowSize = config.getInt("trainer.windowSize")

    // Call train embeddings
    trainEmbeddings(corpusDir, modelOutputPath, minWordFrequency, iterations, layerSize, windowSize)
  }
}
