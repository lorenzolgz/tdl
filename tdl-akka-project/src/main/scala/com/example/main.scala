import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender, Printer}
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{Tweet, AccessToken, ConsumerToken}
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.util.{Failure, Success}

import ExecutionContext.Implicits.global

object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    val consumerToken = ConsumerToken(key = "yAGiXFMZqX8cuHSxK2DTZPSFl", secret = "YVcTBOY6YXGcUamgNjp8Ypajcx9SOiXiYWRpLlyf4emltS624K")
    val accessToken = AccessToken(key = "1102383263400775680-fVPz3JX0hSc1ELeRHwljKABcfIDOIq", secret = "8voFZB0WWQU6Ix8g6eWpTjhrx6qY4LElBOVkocoiy6Qce")  

    val restClient = TwitterRestClient(consumerToken, accessToken)
    val streamingClient = TwitterStreamingClient(consumerToken, accessToken)

    var myfuture: Future[Tweet] = restClient.createTweet("Enanos")
    myfuture.onComplete {
      case Success(r) => println("OK")
      case Failure(r) => println("FAILED")
    }

    for (msg <- restClient.eventsList()){
      println(s"El mensaje fue: ${msg}")
    }

    var anotherFuture: Future[Tweet] = restClient.createDirectMessageAsTweet("Testing", "@AiiiluMG")
    anotherFuture.onComplete {
      case Success(r) => println("enviado msg")
      case Failure(r) => println("fallo envio de msg")
    }


    val printer = system.actorOf(Props(classOf[Printer]), "printer")
    val dataFormatter = system.actorOf(Props(classOf[MovieDataFormatter], printer), "formatter")
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter, printer), "finder")
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")


    var server = new WebServer(dataFormatter, movieFinder, recommender, printer)

    server.startServer("localhost", 8080)

  }
}
