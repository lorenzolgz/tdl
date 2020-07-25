import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender, Printer}
import twitter.{TwitterClient, GetMentions, WakeUp}


object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()

    // MovieDB credentials
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    val printer = system.actorOf(Props(classOf[Printer]), "printer")
    val dataFormatter = system.actorOf(Props(classOf[MovieDataFormatter], printer), "formatter")
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter, printer), "finder")
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")
    val twitterClient = system.actorOf(Props(classOf[TwitterClient]), "twitterClient")

    twitterClient ! WakeUp()

    var server = new WebServer(dataFormatter, movieFinder, recommender, printer)
    server.startServer("localhost", 8080)
  }

}
