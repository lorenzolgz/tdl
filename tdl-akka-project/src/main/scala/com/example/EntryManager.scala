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
      /* Entiendo que matchear a quién responder no haría falta
       * ya que no nos importaría quién sea, en tanto hagamos respondTo ! response
       */ 
      respondTo match {
        case Twitter(user) => {
        //llamar al movierecommender mandandole: req, user
        }
        case Web() => {

        }
        case _ => {}
      }
    }
    case FindMovieRequest(req, respondTo) => {
      respondTo match {
        case Twitter(user) => {
        //llamar al moviefinder mandandole: req, user
        }
        case Web() => {

        }
        case _ => {}
      }
    }
    case _ => {}
  }
}



