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


// Case clases para parsear la respuesta de MovieDB (JSON)

case class Movie(title: String, vote_average: Int, id: Int, overview: String)
case class Page[Movie](page: Int, results: List[Movie])

object MovieDBProtocol extends DefaultJsonProtocol {
  implicit def movieFormat = jsonFormat4(Movie)
  implicit def pageFormat[Movie: JsonFormat] = jsonFormat2(Page.apply[Movie])
}


class MovieDataFormatter() extends Actor {
  import MovieDBProtocol._

  def extractRandomMovies(movies: List[Movie]): ListBuffer[Movie] = {
    val random = new Random
    var randomizedMovies = new ListBuffer[Movie]()
    var i = 0

    while(i <= 30 && i <= movies.length) {
      randomizedMovies += movies(random.nextInt(movies.length))
      i+=1;
    }

    randomizedMovies = randomizedMovies.distinct
    randomizedMovies = randomizedMovies.sortWith((s: Movie, t: Movie) => s.vote_average >= t.vote_average)

    randomizedMovies
  }

  def receive = {
    case movieData: String => {

      var page = movieData.parseJson.convertTo[Page[Movie]]
      val emoji = "\uD83C\uDFAC"

      page.results match {
        case List() =>  sender ! "No se encontraron películas con ese título"
        case _ => {
          var movies = this.extractRandomMovies(page.results)
   
          var i = 0
          var r: String = ""
          while(i <= 2 && i <= movies.length) {
              r = r + s"${emoji} \'${movies(i).title}\' | Valoración: ${movies(i).vote_average}\n"
              i += 1
          }
   
          sender ! r
        }
      }
    }
  }
}


class MovieFinder(system: ActorSystem, val apiKey: String, var formatter: ActorRef) extends Actor {

  var outputManager = system.actorOf(Props(classOf[OutputManager]), "outputManager")

  def receive = {
    case FindMovieRequest(request: String, respondTo: Client) => {

      implicit val actSystem = system; 
      implicit val timeout: Timeout = 5.seconds

      val queryString = Some(s"api_key=${apiKey}&language=es-LA&page=1&include_adult=false&query=${request}")
      val movieDBUri = Uri.from(scheme = "http", host = "api.themoviedb.org", path = "/3/search/movie", queryString = queryString)
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = movieDBUri))

      responseFuture
        .flatMap(Unmarshal(_).to[String])
        .flatMap(ask(formatter, _).mapTo[String])
        .map(finalResult => Recommendation(finalResult, respondTo))
        .pipeTo(outputManager)
    }
    
    case _ => println(s"$self recibio una query que no es del tipo string<")
  }
}
