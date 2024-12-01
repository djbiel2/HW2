import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration


object My_LLM extends App {
  implicit val system: ActorSystem = ActorSystem("DawidLLM")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val logger = LoggerFactory.getLogger(My_LLM.getClass)

  // Load configs
  val config = ConfigFactory.load()
  val model_out_path = sys.env.getOrElse("MODEL_OUTPUT_PATH", config.getString("trainer.model_output_path"))
  logger.info(s"Loading model $model_out_path")

  // Load the trained model
  val word2VecModel: WordVectors = try {
    WordVectorSerializer.readWord2VecModel(model_out_path)
  } catch {
    case e: Exception =>
      logger.error(s"Failed to load $model_out_path", e)
    
      sys.exit(1)
  }
  logger.info("Model loaded successfully")

  // Serve HTML page
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

  // Generate a response
  def response_generator(query: String): String = {
    try {
      val words = query.split(" ")
      val close_words = words.flatMap(word =>
        if (word2VecModel.hasWord(word)) {
          Some(word2VecModel.wordsNearest(word, 5).toArray.mkString(", "))
        } else {
          None
        }
      )
      close_words.mkString(" ")
    } catch {
      case e: Exception =>
        logger.error("Error generating response", e)
        "An error occurred during response."
    }
  }

// Starting server
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
