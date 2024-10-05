import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import java.io.IOException
import org.slf4j.LoggerFactory

class WordCountMapper extends Mapper[LongWritable, Text, Text, IntWritable] {
  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(classOf[WordCountMapper])

  val one = new IntWritable(1)
  val word = new Text()

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, IntWritable]#Context): Unit = {
    try {
      val line = value.toString
      logger.info(s"Processing line: $line")
      val output = mapLine(line)
      output.foreach { case (word, count) =>
        context.write(word, count)
        logger.debug(s"Emitted pair: ($word, $count)")
      }
    } catch {
      case e: Exception =>
        logger.error(s"An error occurred while processing line: ${value.toString}", e)
    }
  }

  // Helper function that performs the actual mapping logic for testing purposes
  def mapLine(line: String): Seq[(Text, IntWritable)] = {
    val tokens = line.split("\\s+")
    tokens.filter(_.nonEmpty).map { token =>
      new Text(token) -> one
    }
  }
}
