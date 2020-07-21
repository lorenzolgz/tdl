import moviedb.{MovieDataFormatter, MovieFinder, MovieRecommender, Printer}
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import akka.stream.ActorMaterializer


object RecomendationService {
  def main(args: Array[String]): Unit = {

    val system = ActorSystem()
    val APIKey = "0f4c286aea338ef131e2ed9b2b522856"

    val dataFormatter = system.actorOf(Props[MovieDataFormatter], "formatter")
    val printer = system.actorOf(Props(classOf[Printer]), "printer")
    val movieFinder = system.actorOf(Props(classOf[MovieFinder], system, APIKey, dataFormatter, printer), "finder")
    val recommender = system.actorOf(Props(classOf[MovieRecommender], system, APIKey, dataFormatter), "recommender")


    var server = new WebServer(dataFormatter, movieFinder, recommender, printer)

    server.startServer("localhost", 8080)

  }
}
