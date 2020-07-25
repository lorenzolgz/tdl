package twitter

import scala.util.{Failure, Success}
import scala.concurrent.{Future, ExecutionContext, Await}
import ExecutionContext.Implicits.global

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{Tweet, AccessToken, ConsumerToken, Event, DirectMessageEventList}
import com.danielasfregola.twitter4s.entities.streaming.{StreamingMessage}

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import commons.{EntryManager, Request, MyTweet, RecommendationRequest}

case class GetMentions()
case class WakeUp()

class TwitterClient(val entryManager: ActorRef) extends Actor {

    val CONSUMER_KEY = "Smqf5X2hPt5brcKfG2MipHXfx"
    val CONSUMER_SECRET = "nngdpENb7QTXQnyJ0gleth8bYTGQGtsa6zBKDb9J7cZfHsaJR0"

    val ACCESS_KEY = "1285759165869236226-XAgIZcuzvkjuLn9pmQ7AZJyCWmKVx0"
    val ACCESS_TOKEN = "snq5qU19DUW5xOfYB891YuUI6AA2FZxeyIGj5Iq9PvXa0"

    val consumerToken = ConsumerToken(key = CONSUMER_KEY, secret = CONSUMER_SECRET)
    val accessToken = AccessToken(key = ACCESS_KEY, secret = ACCESS_TOKEN)

    val streamingClient = TwitterStreamingClient(consumerToken, accessToken)
    val restClient = TwitterRestClient(consumerToken, accessToken)
    //La idea es que haya registro de los tweets ya mandados, pero esto se ejecuta cada vez que se hace reStart
    var oldMentions:List[_] = List()

    //val entryManager = system.actorOf(Props(classOf[EntryManager]), "entryManager")

    def receive = {
      case GetMentions() => {
        var newMentions:List[_] = List()
        var mentions = restClient.mentionsTimeline()
        mentions.onComplete {
          case Success(ratedData) => {
            for (tweetMention <- ratedData.data) {
              var user = tweetMention.user.get
              var mentionid = tweetMention.id
              if(oldMentions.contains(tweetMention) == false){
                println(s"Te mencionó el usuario ${user.screen_name} con el mensaje '${tweetMention.text}'")
                newMentions :: List(tweetMention)
              }
            }
            //entryManager ! newMentions
            oldMentions :: newMentions
          }

          case Failure(e) => println(e)
        }
      }

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
            var name = user.screen_name
            println(s"Repondiendo la mencion (${tweet_id}) del usuario ${user.screen_name}")
            entryManager ! new RecommendationRequest(hashtag, MyTweet(tweet_id, name.toString()))
          }

          case _ => println("se recibio otra cosa")
        }
        streamingClient.filterStatuses(tracks=Seq("MoviesRecommen1"))(printTweetText)

      }
      case _ => println("No recibi nada")

    }

}
