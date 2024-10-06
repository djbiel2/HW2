import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Reducer
import org.slf4j.LoggerFactory
import scala.collection.JavaConverters._
import java.io.IOException

class Word_Count_Reducer extends Reducer[Text, IntWritable, Text, IntWritable] {

 //logging
  private val logger = LoggerFactory.getLogger(classOf[Word_Count_Reducer])
//reduction function
  @throws[IOException]
  @throws[InterruptedException]
  override def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {
    try {
      logger.info(s"Reduction for -${key.toString}")
      val sum = reduce_values(values)
      context.write(key, new IntWritable(sum))
      logger.info(s"Pair-(${key.toString}, $sum)")
    } catch {
      case e: Exception =>
        logger.error(s"Error while reducing", e)
    }
  }

  // Helper function for testing purposes
  def reduce_values(values: java.lang.Iterable[IntWritable]): Int = {
    values.asScala.map(_.get()).sum
  }
}
