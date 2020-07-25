package commons

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer
import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{Tweet, AccessToken, ConsumerToken, Event, DirectMessageEventList}

case class Recommendation(recom: String, respondTo: Client)

class OutputManager extends Actor {

  val system = ActorSystem()

  var CONSUMER_KEY = "Smqf5X2hPt5brcKfG2MipHXfx"
  var CONSUMER_SECRET = "nngdpENb7QTXQnyJ0gleth8bYTGQGtsa6zBKDb9J7cZfHsaJR0"

  var ACCESS_KEY = "1285759165869236226-XAgIZcuzvkjuLn9pmQ7AZJyCWmKVx0"
  var ACCESS_TOKEN = "snq5qU19DUW5xOfYB891YuUI6AA2FZxeyIGj5Iq9PvXa0"

  val consumerToken = ConsumerToken(key = CONSUMER_KEY, secret = CONSUMER_SECRET)
  val accessToken = AccessToken(key = ACCESS_KEY, secret = ACCESS_TOKEN)
  val restClient = TwitterRestClient(consumerToken, accessToken)
  val printer = system.actorOf(Props(classOf[Printer]), "printer")

  case class Twitter(restClient: TwitterRestClient) extends Client
  case class Web() extends Client


  def receive = {
    case Recommendation(recom, MyTweet(mention, user)) => {
      println(recom)
      println(mention)
      println(user)
      restClient.createTweet(
        status=s"@${user} Encontre: \n${recom}",
        in_reply_to_status_id=Option(mention))
    }
    case Recommendation(recom, respondTo: Web) => {
      printer ! recom
    }
    case _ => {
      println("OutputManager recibió algo inválido")
    }
  }
}

class Printer extends Actor {

  def receive = {
    case film:String => {
      println(s"Esta es la película que te recomendamos: ${film}")
    }
  }

}
