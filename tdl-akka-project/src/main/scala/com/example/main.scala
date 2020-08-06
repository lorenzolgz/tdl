import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

import moviedb.{MovieFinder, MovieDataFormatter}
import twitter.{TwitterClient, ListenToMentions}
import commons.{EntryManager, OutputManager}
import webserver.WebServer


object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()

    // MovieDB credentials
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    // Printer Actor for putting responses in console
    //val printer = system.actorOf(Props(classOf[Printer]), "printer")
    // DataFormatter for parsing the MovieDB API responses
    val dataFormatter = system.actorOf(Props(classOf[MovieDataFormatter]), "formatter")
    // MovieFinder based on query
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter), "finder")
    // Universal requests entry manager
    val entryManager = system.actorOf(Props(classOf[EntryManager], movieFinder), "entryManager")
    // Twitter client listening for requests
    val twitterClient = system.actorOf(Props(classOf[TwitterClient], entryManager), "twitterClient")

    twitterClient ! ListenToMentions

    implicit val actorSystem: ActorSystem = system

    var server = new WebServer(entryManager)
    server.startServer("localhost", 8080)
  }

}
