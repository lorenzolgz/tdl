package moviedb

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.{ ask, pipe }
import akka.util.Timeout

import commons.{OutputManager, RecommendationRequest, FindMovieRequest, Client, Recommendation}
import spray.json.{JsValue, JsonFormat, _}
import DefaultJsonProtocol._

import scala.concurrent.{Future, Await, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

import scala.util.Random
import scala.collection.mutable.ListBuffer  


/*
 *  Para parsear un JSON en scala la forma más fácil de hacerlo es
 *  armar case clases que le peguen a cada campo (no hace falta utilizarlos todos)
 *  en este caso creamos uno para la página en sí (que contiene el número y una lista
 *  de películas); y otro para la película (que contiene el título, autor, etc.)
 *  Además hay que crear un protocolo de unmarshalling para poder utilizar estas
 *  cases clases en este caso es MovieDBProtocol
 */


case class Movie(title: String, vote_average: Int, id: Int, overview: String)
case class Page[Movie](page: Int, results: List[Movie])

object MovieDBProtocol extends DefaultJsonProtocol {
  implicit def movieFormat = jsonFormat4(Movie)
  implicit def pageFormat[Movie: JsonFormat] = jsonFormat2(Page.apply[Movie])
}

class MovieDataFormatter() extends Actor {
  import MovieDBProtocol._
  implicit val timeout: Timeout = 5.seconds

  def receive = {
    case movieData: String => {

      var page = movieData.parseJson.convertTo[Page[Movie]]

      var r: String = ""
      val emoji = "\uD83C\uDFAC"
      var i = 0
      val random = new Random

      var movies = new ListBuffer[Movie]()

      while (i <= 30 && i <= page.results.length) {
        movies += page.results(random.nextInt(page.results.length))
        i+=1;
      }
      
      movies = movies.distinct
      movies.sortWith((s: Movie, t: Movie) => s.vote_average >= t.vote_average)

      i = 0
      while(i <= 2 && i <= movies.length) {
        var movie = movies(i)
        r = r + s"${emoji} \'${movie.title}\' | Valoración: ${movie.vote_average}\n"
        i += 1
      }

      sender ! r

    }
  }
}

class MovieFinder(system: ActorSystem, val apiKey: String, var formatter: ActorRef) extends Actor {
  var outputManager = system.actorOf(Props(classOf[OutputManager]), "outputManager")

  def receive = {
    case FindMovieRequest(request: String, respondTo: Client) => {

      implicit val actSystem = system; // Para el Http()
      implicit val timeout: Timeout = 5.seconds

      val queryString = Some(s"api_key=${apiKey}&language=es-LA&page=1&include_adult=false&query=${request}")
      val movieDBUri = Uri.from(scheme = "http", host = "api.themoviedb.org", path = "/3/search/movie", queryString = queryString)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = movieDBUri))

      responseFuture.onComplete {

        case Success(res) => {
          println(s"Response for key=${request} with code=${res.status.intValue()}")

          val stringFuture: Future[String] = Unmarshal(res).to[String]

          val formattedMovies: Future[String] = ask(formatter, Await.result(stringFuture, 5 seconds)).mapTo[String]

          formattedMovies onComplete {
            case Success(value) => {
              //println(s"recibido, enviando $value")
              //Future {
              //  value
              //}.pipeTo(sender)

              //Se lo envío al outputManager
              outputManager ! Recommendation(value,respondTo)

            }
            case Failure(t) => println(s"Ocurrio un error esperando respuesta de $formatter")

          }

        }
        case Failure(_)   => sys.error("Ocurrio un error al esperar la respuesta de la API")
      }
    }
    case _ => println(s"$self recibio una query que no es del tipo string<")
  }

}
