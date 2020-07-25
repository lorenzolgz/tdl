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


class WebServer(val formatter: ActorRef, val finder: ActorRef, val recommender: ActorRef) extends HttpApp {

  implicit val timeout: Timeout = 5.seconds

  override def routes: Route =
    concat(
      pathSingleSlash {
        get {
          val movieResults: Future[String] = ask(finder, "2048").mapTo[String]

          // FIXME: esto por algún motivo nunca llega y se termina enviando a deadLetters, el buzón default para los mensajes
          //        entre actores que nunca llegaron
          onComplete(movieResults) {
            case Success(value) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, value))
            case Failure(res) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1> Ocurrió un error <h1>"))
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
