import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
//Hadoop setup for distributed processing
object Hadoop {
  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      println("Hadoop input  output ")
      System.exit(-1)
    }
    //configs and job instance
    val conf = new Configuration()
    val job = Job.getInstance(conf, "Word Count")


    job.setJarByClass(classOf[Word_Count_Mapper])
    //setting mapping and reducer classes

    job.setMapperClass(classOf[Word_Count_Mapper])
    job.setReducerClass(classOf[Word_Count_Reducer])

    //setting output classes
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[IntWritable])

    // setting input and ouputclases
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))

    // run the job but exit if unsuccessful
    System.exit(if (job.waitForCompletion(true)) 0 else 1)
  }
}
