import java.io.File
import scala.io.Source
import scala.collection.JavaConverters._
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.{Encoding, EncodingRegistry, EncodingType, IntArrayList}
import org.slf4j.LoggerFactory

import java.io.PrintWriter
import com.typesafe.config.ConfigFactory

object CorpusSharder {
  def splitCorpusIntoShards(inputPath: String, shardSize: Int, outputDir: String): Unit = {
    val outputDirectory = new File(outputDir)
    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs()
    }

    val corpusLines = Source.fromFile(inputPath).getLines()

    var shardText = new StringBuilder
    var shardIndex = 0

    for (line <- corpusLines) {
      shardText.append(line).append(" ")

      if (shardText.length >= shardSize) {
        writeShard(shardText.toString(), outputDir, shardIndex)
        shardIndex += 1
        shardText = new StringBuilder
      }
    }

    if (shardText.nonEmpty) {
      writeShard(shardText.toString(), outputDir, shardIndex)
    }
  }

  private def writeShard(shard: String, outputDir: String, shardIndex: Int): Unit = {
    val writer = new PrintWriter(s"$outputDir/shard_$shardIndex.txt")
    writer.write(shard)
    writer.close()
  }
}

object Main {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(Main.getClass)

  def main(args: Array[String]): Unit = {
    // Load configuration
    val config = ConfigFactory.load()

    try {
      // Step 1: Split the corpus into shards
      val inputPath = config.getString("main.corpusPath")
      val shardSize = config.getInt("main.shardSize")
      val outputDirShards = config.getString("main.outputDir.shards")

      logger.info(s"Splitting corpus at $inputPath into shards of size $shardSize characters, saving to $outputDirShards.")
      CorpusSharder.splitCorpusIntoShards(inputPath, shardSize, outputDirShards)
      logger.info(s"Corpus has been split into shards and saved to: $outputDirShards")

      // Step 2: Tokenize each shard
      val shardDirectory = new File(outputDirShards)
      val shardFiles = shardDirectory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))

      val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
      val encoding: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

      shardFiles.foreach { shardFile =>
        logger.info(s"Tokenizing shard: ${shardFile.getName}")
        val shardText = Source.fromFile(shardFile.getAbsolutePath).mkString
        val tokenized: IntArrayList = encoding.encode(shardText)

        // Convert IntArrayList to Array[Int] (this may be used for further processing)
        val tokensArray: Array[Int] = tokenized.toArray
        logger.info(s"Tokenizing complete for shard: ${shardFile.getName}")
      }

      // Load Trainer Configurations
      val minWordFrequency = config.getInt("trainer.minWordFrequency")
      val iterations = config.getInt("trainer.iterations")
      val layerSize = config.getInt("trainer.layerSize")
      val windowSize = config.getInt("trainer.windowSize")

      // Step 3: Train Word2Vec Model to Generate Token Embeddings
      val modelOutputPath = config.getString("main.outputDir.model")
      logger.info(s"Training Word2Vec model using shards in $outputDirShards, saving to $modelOutputPath.")
      EmbeddingTrainer.trainEmbeddings(outputDirShards, modelOutputPath, minWordFrequency, iterations, layerSize, windowSize)
      logger.info(s"Word2Vec model has been trained and saved to: $modelOutputPath")

      // Step 4: Generate Vocabulary with Frequency and Save as CSV
      val vocabularyOutputPath = config.getString("main.outputDir.vocabulary")
      logger.info(s"Generating vocabulary with frequency from shards in $outputDirShards, saving to $vocabularyOutputPath.")
      VocabularyWriter.generateVocabularyWithFrequency(outputDirShards, vocabularyOutputPath)
      logger.info(s"Vocabulary with frequency has been saved to: $vocabularyOutputPath")

      // Step 5: Evaluate Trained Embeddings
      val wordsToEvaluate = config.getStringList("wordsToEvaluate").asScala.toList

      logger.info(s"Evaluating embeddings from model at $modelOutputPath.")
      EmbeddingEvaluator.evaluateEmbeddings(modelOutputPath, wordsToEvaluate)
      logger.info("Embedding evaluation has been completed.")
    } catch {
      case e: Exception =>
        logger.error("An error occurred during the main process.", e)
    }
  }
}
