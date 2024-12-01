import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import scala.util.Random
import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration
import scala.util.control.Breaks._



object My_LLM extends App {
  implicit val system: ActorSystem = ActorSystem("DawidLLM")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val logger = LoggerFactory.getLogger(My_LLM.getClass)

  // load configs
  val config = ConfigFactory.load()
  val model_out_path = sys.env.getOrElse("MODEL_OUTPUT_PATH", config.getString("trainer.model_output_path"))
  logger.info(s"Loading model $model_out_path")

  // load my model
  val word2VecModel: WordVectors = try {
    WordVectorSerializer.readWord2VecModel(model_out_path)
  } catch {
    case e: Exception =>
      logger.error(s"Failed to load $model_out_path", e)
    
      sys.exit(1)
  }
  logger.info("Model loaded successfully")

  // server the html page
  val route =
    path("") {
      get {
        getFromResource("static/index.html")
      }
    } ~
      pathPrefix("static") {
        getFromResourceDirectory("static")
      } ~
      path("api" / "generate") {
        post {
          entity(as[String]) { query =>
            val response = response_generator(query)
            complete(StatusCodes.OK, response)
          }
        }
      }

  // generate a resposne
  def response_generator(query: String): String = {
    try {
      // preprocess the query
      val cleaned_query = query.toLowerCase.replaceAll("[^a-z\\s]", "").trim
      val initial_words = cleaned_query.split("\\s+").filter(word2VecModel.hasWord).toList

      if (initial_words.isEmpty) {
        "Please ask away!"
      } else {
        val random = new Random()
        val max_sentence_length = 5 + random.nextInt(6) // random length between 5 and 10
        var sentence_words = initial_words

        // generate
        breakable {
          while (sentence_words.length < max_sentence_length) {
            val last_word = sentence_words.last
            // get the 10 nearest words to the last word
            val raw_words: java.util.Collection[String] = word2VecModel.wordsNearest(last_word, 10)
            val scala_words: Seq[String] = raw_words.asScala.toSeq
              .map(_.replaceAll("[^a-zA-Z]", "").toLowerCase)
              .filterNot(_.isEmpty)
              .filter(word2VecModel.hasWord) // ensure the word exists in the model

            // if no related words are found break the loop
            if (scala_words.isEmpty) {
              break
            }

            // select the next word
            val next_word = scala_words.head
            sentence_words = sentence_words :+ next_word
          }
        }

        // build the sentence
      val sentence = sentence_words.mkString(" ")
      
      // capitalize and add a period at the end
      val fin_sentence = sentence.capitalize + "."
      
      fin_sentence
    } 
  } catch {
    case e: Exception =>
      logger.error("Error during generating response", e)
      "Error during response"
  }
}

// start server
  val bindings = Http().newServerAt("0.0.0.0", 8080).bind(route)
  bindings.onComplete {
    case scala.util.Success(binding) =>
      logger.info(s"Server successfully started at ${binding.localAddress}")
    case scala.util.Failure(exception) =>
      logger.error("Failed at binding server", exception)
      system.terminate()
  }

  
  Await.result(system.whenTerminated, Duration.Inf)
}
