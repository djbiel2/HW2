import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.FileSentenceIterator
import org.deeplearning4j.text.sentenceiterator.SentenceIterator
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory
import java.io.File

object Embedding_Trainer {

  // Creating a logger
  private val logger = LoggerFactory.getLogger(Embedding_Trainer.getClass)

//Train the embeddings and then save the model
  def train_embeddings(corpus_dir: String, model_output_path: String, min_word_frequency: Int, num_iterations: Int, layer_size: Int, context_window_size: Int): Unit = {
    try {
      // need to interate over sentences from teh corpus
      logger.info(s"Loading data from $corpus_dir")
      val corpus_directory = new File(corpus_dir)
      val sentence_iterator: SentenceIterator = new FileSentenceIterator(corpus_directory)

      // Use DefaultTokenizerFactory to tokenize the sentences
      logger.info("Initialize tokenizer")
      val tokenizer_factory = new DefaultTokenizerFactory()

      // create the Word2Ved model
      logger.info(s"Creating model")
      val word2vec_model = new Word2Vec.Builder()
        .minWordFrequency(min_word_frequency)
        .iterations(num_iterations)
        .layerSize(layer_size)
        .windowSize(context_window_size)
        .iterate(sentence_iterator)
        .tokenizerFactory(tokenizer_factory)
        .build()

      // training the model
      logger.info("Training model now")
      word2vec_model.fit()
      logger.info("Training was successful")

      // Saving the model to the output
      WordVectorSerializer.writeWord2VecModel(word2vec_model, new File(model_output_path))
      logger.info("Model saved")
    }
    catch {
      case e: Exception =>
        logger.error("Error during training", e)
    }
  }

  def main(args: Array[String]): Unit = {
    // Load configs
    val config = ConfigFactory.load()
    val corpus_dir = config.getString("trainer.corpus_dir")
    val model_output_path = config.getString("trainer.model_output_path")
    val min_word_frequency = config.getInt("trainer.min_word_frequency")
    val num_iterations = config.getInt("trainer.num_iterations")
    val layer_size = config.getInt("trainer.layer_size")
    val context_window_size = config.getInt("trainer.context_window_size")

    // call embeddings trainer
    train_embeddings(corpus_dir, model_output_path, min_word_frequency, num_iterations, layer_size, context_window_size)


  }
}
