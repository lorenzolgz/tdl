package commons

import akka.actor.{Actor, ActorRef}

import com.danielasfregola.twitter4s.entities.{Tweet}

case class RecommendationRequest(request: String, respondTo: Client) extends Request
case class FindMovieRequest(request: String, respondTo: Client) extends Request

class EntryManager(SearchService: ActorRef) extends Actor {

  def receive = {
    case RecommendationRequest(req, MyTweet(mention,user)) => {
      SearchService ! FindMovieRequest(req, new MyTweet(mention,user))
    }

    case RecommendationRequest(req, client: WebClient) => {
      SearchService ! FindMovieRequest(req, client)
    }

    case _ => {
      println("EntryManager recibió algo inválido")
    }
  }
}
