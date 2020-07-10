import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import scala.concurrent.Future
import scala.util.{ Failure, Success }

class buscadorPeliculas(system: ActorSystem, materializer: ActorMaterializer) extends Actor {

  def receive = {
    case query:String => {

      implicit val Actsystem = system;
      implicit val Actmat = materializer;
      implicit val executionContext = system.dispatcher
      val movieQuery = query
      val queryString = Some("api_key=0f4c286aea338ef131e2ed9b2b522856&language=en-US&page=1&include_adult=false&query=" ++ movieQuery)
      val movieDBUri = Uri.from(scheme = "http", host = "api.themoviedb.org", path = "/3/search/movie", queryString = queryString)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = movieDBUri))

      responseFuture.onComplete {
        case Success(res) => {
          println("Llego respuesta de la API para: " ++ query)
          val stringFuture: Future[String] = Unmarshal(res).to[String]
          println(Await.result(stringFuture, 5 seconds))
        }
        case Failure(_)   => sys.error("Ocurrio un error al esperar la respuesta de la API")
      }
    }
    case _ => println("Buscador recibio una query que no es del tipo string<")
  }

}

object Client {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    val buscador = system.actorOf(Props(classOf[buscadorPeliculas], system, materializer), "buscador")
    buscador ! "robot"
    buscador ! "alien"
  }
}
