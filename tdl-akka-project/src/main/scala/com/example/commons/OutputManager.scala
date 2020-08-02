package commons

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{Tweet}

case class Recommendation(recom: String, respondTo: Client)

class OutputManager extends Actor {

  val system = ActorSystem()
  val printer = system.actorOf(Props(classOf[Printer]), "printer")

  val restClient = TwitterRestClient()

  def receive = {
    case Recommendation(recom, MyTweet(mention, user)) => {
      println(recom)
      println(mention)
      println(user)
      restClient.createTweet(
        status=s"@${user} Encontre: \n${recom}",
        in_reply_to_status_id=Option(mention))
    }
    case Recommendation(recom, respondTo: WebClient) => {
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
