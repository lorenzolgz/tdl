import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender, Printer}
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{Tweet, AccessToken, ConsumerToken, Event, DirectMessageEventList}
import scala.concurrent.{Future, ExecutionContext, Await}
import scala.util.{Failure, Success}

import ExecutionContext.Implicits.global

object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()
    // OJO: esta es para MovieDB
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    // Estas son para twitter
    val CONSUMER_KEY = "Smqf5X2hPt5brcKfG2MipHXfx"
    val CONSUMER_SECRET = "nngdpENb7QTXQnyJ0gleth8bYTGQGtsa6zBKDb9J7cZfHsaJR0"

    val ACCESS_KEY = "1285759165869236226-XAgIZcuzvkjuLn9pmQ7AZJyCWmKVx0"
    val ACCESS_TOKEN = "snq5qU19DUW5xOfYB891YuUI6AA2FZxeyIGj5Iq9PvXa0"

    val consumerToken = ConsumerToken(key = CONSUMER_KEY, secret = CONSUMER_SECRET)
    val accessToken = AccessToken(key = ACCESS_KEY, secret = ACCESS_TOKEN)

    val restClient = TwitterRestClient(consumerToken, accessToken)
    val streamingClient = TwitterStreamingClient(consumerToken, accessToken)


    //Recibe las mensiones
    var myfuture = restClient.mentionsTimeline()
    myfuture.onComplete {
      case Success(r) =>  for (md <- myfuture){
                            println(s"El mensaje fue: ${md}")
                          }
      case Failure(r) => println(r)
    }

    //Envía tweet
    val user = "@AiiiluMG"
    val tweet = s"Estas son las películas que te recomiendo: "

    var mdToSend = restClient.createDirectMessageAsTweet(tweet, user)
    mdToSend.onComplete {
      case Success(r) => println("Mensaje enviado")
      case Failure(r) => println("Falló envío de mensaje")
    }

    val printer = system.actorOf(Props(classOf[Printer]), "printer")
    //val twitter = system.actorOf(Props(classOf[SenderMDTwitter]), "senderMDTwitter")
    val dataFormatter = system.actorOf(Props(classOf[MovieDataFormatter], printer), "formatter")
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter, printer), "finder")
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")


    var server = new WebServer(dataFormatter, movieFinder, recommender, printer)

    server.startServer("localhost", 8080)

  }
}
