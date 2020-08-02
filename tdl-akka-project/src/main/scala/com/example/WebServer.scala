import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import ExecutionContext.Implicits.global

import commons.{EntryManager, RecommendationRequest, WebClient}


class WebServer(val entryManager: ActorRef) extends HttpApp {

  implicit val timeout: Timeout = 5.seconds

  override def routes: Route =
    concat(
      pathSingleSlash {
        get {
          val movieResults: Future[String] = ask(entryManager, RecommendationRequest("dog", WebClient(null))).mapTo[String]

          onComplete(movieResults) {
            case Success(value) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, value))
            case Failure(res) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "Ocurrió un error"))
          }
        }
      },
      path("test") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h2> Hello world! </h2>"))
        }
      }
    )
  }
