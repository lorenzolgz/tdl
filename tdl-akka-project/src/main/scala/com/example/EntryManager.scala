import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

class EntryManager(val RecomendationService: ActorRef, val SearchService: ActorRef) extends Actor {
  abstract class Client
  abstract class Request

  case class Twitter(user: String) extends Client
  case class Web() extends Client

  case class RecommendationRequest(request: String, respondTo: Client) extends Request
  case class FindMovieRequest(request: String, respondTo: Client) extends Request

  def receive = {
    case RecommendationRequest(req, respondTo) => {
      
    }
    case FindMovieRequest(req, respondTo) => {

    }
    case _ => {
      println("EntryManager recibió algo inválido")
    }
  }
}
