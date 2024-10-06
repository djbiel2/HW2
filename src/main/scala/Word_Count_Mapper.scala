import org.apache.hadoop.io.{IntWritable, LongWritable, Text}
import org.apache.hadoop.mapreduce.Mapper
import java.io.IOException
import org.slf4j.LoggerFactory

class Word_Count_Mapper extends Mapper[LongWritable, Text, Text, IntWritable] {
 //logger
  private val logger = LoggerFactory.getLogger(classOf[Word_Count_Mapper])
//mapping function
  val one = new IntWritable(1)
  val word = new Text()

  @throws[IOException]
  @throws[InterruptedException]
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, IntWritable]#Context): Unit = {
    try {
      val line = value.toString
      logger.info(s"Line- $line")
      val output = map_line(line)
      output.foreach { case (word, count) =>
        context.write(word, count)
        logger.debug(s"Pair- ($word, $count)")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Processing line error", e)
    }
  }

  // helper function for testing purpuses
  def map_line(line: String): Seq[(Text, IntWritable)] = {
    val tokens = line.split("\\s+")
    tokens.filter(_.nonEmpty).map { token =>
      new Text(token) -> one
    }
  }
}
