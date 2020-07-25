package commons

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.danielasfregola.twitter4s.entities.{Tweet}
import akka.stream.ActorMaterializer

case class RecommendationRequest(request: String, respondTo: MyTweet) extends Request
case class FindMovieRequest(request: String, respondTo: MyTweet) extends Request

class EntryManager(val RecomendationService: ActorRef, val SearchService: ActorRef) extends Actor {

  case class Twitter(user: String) extends Client
  case class Web() extends Client


  def receive = {
    case RecommendationRequest(req, MyTweet(mention,user)) => {
      SearchService ! FindMovieRequest(req, new MyTweet(mention,user))
    }
    case _ => {
      println("EntryManager recibió algo inválido")
    }
  }
}
