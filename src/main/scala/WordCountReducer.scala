import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._


import java.io.IOException

class WordCountReducer extends Reducer[Text, IntWritable, Text, IntWritable] {

  // Create a logger for this class
  private val logger = LoggerFactory.getLogger(classOf[WordCountReducer])

  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {
    try {
      logger.info(s"Starting reduction for key: ${key.toString}")
      val sum = reduceValues(values)
      context.write(key, new IntWritable(sum))
      logger.info(s"Emitted pair: (${key.toString}, $sum)")
    } catch {
      case e: Exception =>
        logger.error(s"An error occurred while reducing key: ${key.toString}", e)
    }
  }

  // Helper function for testing purposes
  def reduceValues(values: java.lang.Iterable[IntWritable]): Int = {
    values.asScala.map(_.get()).sum
  }
}
