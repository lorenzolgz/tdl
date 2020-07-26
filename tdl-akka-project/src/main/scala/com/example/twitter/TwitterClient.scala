package twitter

import com.danielasfregola.twitter4s.{TwitterRestClient, TwitterStreamingClient}
import com.danielasfregola.twitter4s.entities.streaming.{StreamingMessage}
import com.danielasfregola.twitter4s.entities.{Tweet}

import akka.actor.{Actor, ActorRef}

import commons.{EntryManager, MyTweet, RecommendationRequest}

case class ListenToMentions()

class TwitterClient(val entryManager: ActorRef) extends Actor {

    val streamingClient = TwitterStreamingClient()

    def receive = {
      case ListenToMentions => {
        def printTweetText: PartialFunction[StreamingMessage, Unit] = {

          case tweet: Tweet => {

            var hashtags = tweet.entities.map(_.hashtags).getOrElse(List.empty)
            var hashtag = ""
            if(hashtags.size > 0){
              hashtag = hashtags(0).text
            }
            println(s"Hashtag: ${hashtag}")

            var user = tweet.user.get
            println(s"Repondiendo la mencion (${tweet.id}) del usuario ${user.screen_name}")
            entryManager ! new RecommendationRequest(hashtag, MyTweet(tweet.id, user.screen_name.toString()))
          }

          case _ => println("Se recibió otra cosa")
        }
        streamingClient.filterStatuses(tracks=Seq("MoviesRecommen1"))(printTweetText)

      }
      case _ => println("No recibi nada")

    }

}
