import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender, SimpleMovieDataFormatter}
import twitter.{TwitterClient, ListenToMentions}
import commons.{EntryManager, OutputManager}


object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()

    // MovieDB credentials
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    // Printer Actor for putting responses in console
    //val printer = system.actorOf(Props(classOf[Printer]), "printer")
    // DataFormatter for parsing the MovieDB API responses
    val dataFormatter = system.actorOf(Props(classOf[SimpleMovieDataFormatter]), "formatter")
    // MovieFinder based on query
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter), "finder")
    // Movie Recommender based on ID
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")
    // Universal requests entry manager
    val entryManager = system.actorOf(Props(classOf[EntryManager], recommender, movieFinder), "entryManager")
    // Twitter client listening for requests
    val twitterClient = system.actorOf(Props(classOf[TwitterClient], entryManager), "twitterClient")

    twitterClient ! ListenToMentions

    var server = new WebServer(entryManager)
    server.startServer("localhost", 8080)
  }

}
