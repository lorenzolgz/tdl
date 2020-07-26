package twitter

import scala.util.{Failure, Success}
import scala.concurrent.{Future, ExecutionContext, Await}
import ExecutionContext.Implicits.global

import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.danielasfregola.twitter4s.entities.{Tweet, AccessToken, ConsumerToken, Event, DirectMessageEventList}
import com.danielasfregola.twitter4s.entities.streaming.{StreamingMessage}

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import commons.{EntryManager, Request, MyTweet, RecommendationRequest}

case class WakeUp()

class TwitterClient(val entryManager: ActorRef) extends Actor {

    val streamingClient = TwitterStreamingClient()
    val restClient = TwitterRestClient()

    def receive = {
      case WakeUp() => {
        val emoji = "\uD83D\uDE01"
        def printTweetText: PartialFunction[StreamingMessage, Unit] = {
          case tweet: Tweet => {
            println(s"Procesando: ${tweet.text}")
            var hashtags = tweet.entities.map(_.hashtags).getOrElse(List.empty)
            var hashtag = ""
            if(hashtags.size > 0){
              hashtag = hashtags(0).text
            }
            println(s"Hashtag: ${hashtag}")
            var tweet_id: Long = tweet.id
            var user = tweet.user.get
            println(s"Repondiendo la mencion (${tweet_id}) del usuario ${user.screen_name}")
            entryManager ! new RecommendationRequest(hashtag, MyTweet(tweet_id, user.screen_name.toString()))
          }

          case _ => println("se recibio otra cosa")
        }
        streamingClient.filterStatuses(tracks=Seq("MoviesRecommen1"))(printTweetText)

      }
      case _ => println("No recibi nada")

    }

}
