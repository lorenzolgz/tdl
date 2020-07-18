import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender}
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer

object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    val dataFormatter = system.actorOf(Props[MovieDataFormatter], "formatter")
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter), "finder")
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")

    movieFinder ! "robot"
    recommender ! "2048"

    var server = new WebServer()
    server.startServer("localhost", 8080)             
  }
}
