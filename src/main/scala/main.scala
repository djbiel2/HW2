import java.io.File
import scala.io.Source
import scala.collection.JavaConverters._
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import org.slf4j.LoggerFactory
import java.io.PrintWriter
import com.typesafe.config.ConfigFactory

//object to handle shard operations
object shards_fun {
  def split_into_shards(input_path: String, shard_size: Int, output_dir: String): Unit = {
    //create output dir for the shards
    val output_directory = new File(output_dir)
    if (!output_directory.exists()) {
      output_directory.mkdirs()
    }
//read from the corpus
    val corpus_lines = Source.fromFile(input_path).getLines()
//initialize variables for the shards
    var shard_text = new StringBuilder
    var shard_index = 0
//iterate over every line in the corpus
    for (line <- corpus_lines) {
      shard_text.append(line).append(" ")
//if shard exceeds the limit then write it to a file
      if (shard_text.length >= shard_size) {
        write_shard(shard_text.toString(), output_dir, shard_index)
        shard_index += 1
        shard_text = new StringBuilder
      }
    }
//keep writing till it is empty
    if (shard_text.nonEmpty) {
      write_shard(shard_text.toString(), output_dir, shard_index)
    }
  }
//writing shard to output directory
  private def write_shard(shard: String, output_dir: String, shard_index: Int): Unit = {
    val writer = new PrintWriter(s"$output_dir/shard_$shard_index.txt")
    writer.write(shard)
    writer.close()
  }
}

object Main {

//logger
  private val logger = LoggerFactory.getLogger(Main.getClass)

  def main(args: Array[String]): Unit = {
    // Load configs
    val config = ConfigFactory.load()

    try {
      // Split the text into shards
      val corpus_path = config.getString("main.corpus_path")
      val shard_size = config.getInt("main.shard_size")
      val output_dir_shards = config.getString("main.output_dir.shards_path")

      logger.info(s"Splitting text into shards of size $shard_size characters")
      shards_fun.split_into_shards(corpus_path, shard_size, output_dir_shards)
      logger.info(s"Text has been split into shards and was saved.")

      //Tokenize each shard
      val shard_directory = new File(output_dir_shards)
      val shard_files = shard_directory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))

      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

      shard_files.foreach { shard_file =>
        logger.info(s"Tokenizing shard- ${shard_file.getName}")
        val shard_text = Source.fromFile(shard_file.getAbsolutePath).mkString
        val tokenized: IntArrayList = encoding.encode(shard_text)

        // Convert to  Array[Int]
        val tokens_array: Array[Int] = tokenized.toArray
        logger.info(s"Tokenizing finished for shard- ${shard_file.getName}")
      }

      // Load trainer
      val min_word_frequency = config.getInt("trainer.min_word_frequency")
      val num_iterations = config.getInt("trainer.num_iterations")
      val layer_size = config.getInt("trainer.layer_size")
      val context_window_size = config.getInt("trainer.context_window_size")

      // Train model to get tokens embeddings
      val model_output_path = config.getString("main.output_dir.model_output")
      logger.info(s"Training model using shards in $output_dir_shards")
      Embedding_Trainer.train_embeddings(output_dir_shards, model_output_path, min_word_frequency, num_iterations, layer_size, context_window_size)
      logger.info(s"Model has been trained succesfully")

      // create a vocabulary of all diff words and put it in the output direcotry
      val vocabulary_output_path = config.getString("main.output_dir.vocabulary_output")
      logger.info(s"Creating the vocabulary")
      Vocabulary_Writer.generate_vocabulary_with_frequency(output_dir_shards, vocabulary_output_path)
      logger.info(s"Vocabulary succesfully created")

      // Evaluate those embeddings
      val words_to_evaluate = config.getStringList("words_to_evaluate").asScala.toList

      logger.info(s"Evaluating embeddings from model at $model_output_path.")
      Embedding_Evaluator.evaluate_embeddings(model_output_path, words_to_evaluate)
      logger.info("Embedding evaluation has been completed")

      logger.info("Starting HTTP server")
      My_LLM.main(Array.empty)
      logger.info("HTTP server started successfully")
    } catch {
      case e: Exception =>
        logger.error("An error occurred during the main process", e)
    }
  }
}
