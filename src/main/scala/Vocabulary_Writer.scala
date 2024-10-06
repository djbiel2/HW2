import java.io.{File, PrintWriter}
import scala.io.Source
import scala.collection.mutable
import org.slf4j.LoggerFactory
import com.typesafe.config.ConfigFactory

object Vocabulary_Writer {

//logger
  private val logger = LoggerFactory.getLogger(Vocabulary_Writer.getClass)


// Create a vocabulary with frequencies and then save it to output

  def generate_vocabulary_with_frequency(shard_dir: String, output_file_path: String): Unit = {
    try {
      // use map to store frequenices
      val word_frequency = mutable.Map[String, Int]().withDefaultValue(0)


      val shard_directory = new File(shard_dir)
      if (!shard_directory.exists() || !shard_directory.isDirectory) {
        logger.error(s"Shard file not found, error")
        return
      }

      val shard_files = shard_directory.listFiles().filter(_.isFile).filter(_.getName.startsWith("shard_"))

      if (shard_files.isEmpty) {
        logger.warn(s"No shard files in file, error")
        return
      }

      logger.info(s"Processing shards")

      // go though each shard and count freqencies, make sure that only letters within words are counted not ,.;: etc.
      shard_files.foreach { shard_file =>
        logger.info(s"Shard- ${shard_file.getName}")
        val source = Source.fromFile(shard_file)
        source.getLines().foreach { line =>
          val words = line.split("\\s+").map(_.replaceAll("[^a-zA-Z]", "").toLowerCase).filter(_.nonEmpty)

          words.foreach { word =>
            word_frequency(word) += 1
          }
        }
        source.close()
      }

      //Write the vocabulary to output
      logger.info(s"Printing vocabulary with word freqencies to $output_file_path")
      val writer = new PrintWriter(new File(output_file_path))
      writer.println("Word,Frequency")
      word_frequency.foreach {
        case (word, frequency) =>
          writer.println(s"$word,$frequency")
      }
      writer.close()
      logger.info("Vocabulary creating successful")
    } catch {
      case e: Exception =>
        logger.error("Vocabulary creation error", e)
    }
  }



  def main(args: Array[String]): Unit = {
  //load configs
    val config = ConfigFactory.load()
    val shard_dir = config.getString("vocabulary_writer.shard_dir")
    val output_file_path = config.getString("vocabulary_writer.output_file_path")

   //generate vocabulary with freqency
    generate_vocabulary_with_frequency(shard_dir, output_file_path)
  }
}
