import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContextExecutor

object My_LLM extends App {
  implicit val system: ActorSystem = ActorSystem("DawidLLM")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val logger = LoggerFactory.getLogger(My_LLM.getClass)

  // Load configs
  val config = ConfigFactory.load()
  val model_out_path = config.getString("trainer.model_output_path")

  // Load the trained model
  val word2VecModel: WordVectors = WordVectorSerializer.readWord2VecModel(model_out_path)
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
Http().newServerAt("0.0.0.0", 80).bind(route).onComplete {
    case scala.util.Success(binding) =>
      logger.info(s"Server successfully started at ${binding.localAddress}")
    case scala.util.Failure(exception) =>
      logger.error("Failed at binding server", exception)
  }
}
