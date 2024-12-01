import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import scala.util.Random
import scala.jdk.CollectionConverters._
import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration


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
    // split query
    val words = query.split("\\s+")
    
    // get close words from my model 
    val close_words = words.flatMap { word =>
      if (word2VecModel.hasWord(word)) {
        //get 10 closest words
        word2VecModel.wordsNearest(word, 10).asScala
      } else {
        Seq.empty[String]
      }
    }

    if (close_words.nonEmpty) {
      val random = new Random()
      
      // get a random words
      val number_words = 5 + random.nextInt(6)
      
      // 
      val sel_words = random.shuffle(close_words).take(number_words)
      
      // make them into a sentence
      val sentence = sel_words.mkString(" ")
      
      // capitalize and add a period at the end
      val fin_sentence = sentence.capitalize + "."
      
      fin_sentence
    } else {
      "Please try again later, Dawid is OoO."
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
