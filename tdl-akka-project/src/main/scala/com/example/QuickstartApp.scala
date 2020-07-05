import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._


import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Client {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "https://api.themoviedb.org/3/search/movie?api_key=0f4c286aea338ef131e2ed9b2b522856&language=en-US&page=1&include_adult=false&query=enano"))

    responseFuture
      .onComplete {
        case Success(res) => {
            println("Llego respuesta de la API")
            val stringFuture: Future[String] = Unmarshal(res).to[String]
            println(Await.result(stringFuture, 5 seconds))
        }
        case Failure(_)   => sys.error("something wrong")
      }
  }
}
