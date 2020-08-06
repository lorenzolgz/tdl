package commons

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{Tweet}

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }

case class Recommendation(recom: String, respondTo: Client)

class OutputManager extends Actor {

  val system = ActorSystem()
  val printer = system.actorOf(Props(classOf[Printer]), "printer")

  val restClient = TwitterRestClient()

  def receive = {
    case Recommendation(recom, MyTweet(mention, user)) => {
      println(recom)
      println(mention)
      println(user)
      restClient.createTweet(
        status=s"@${user} Te recomiendo: \n${recom}",
        in_reply_to_status_id=Option(mention))
    }
    case Recommendation(recom, respondTo: WebClient) => {
      var template = """
      <head>
        <link rel="stylesheet" href="resources/styles.css">
      </head>
      <h1 class="titulo">
        <img src="resources/movie.png" style="width: 55; height: 55">
        Recomendador de películas 
        <img src="resources/movie.png" style="width: 55; height: 55"> 
      </h1>

      <h2> Estas son las películas que te recomendamos: </h2><br><br>"""

      template = template + "<h1>"
      template = template + recom.replace("\n", "</br>")
      template = template + "</h1>"

      respondTo.complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, template))
      context.stop(self)
    }
    case _ => {
      println("OutputManager recibió algo inválido")
    }
  }
}

class Printer extends Actor {

  def receive = {
    case film:String => {
      println(s"Esta es la película que te recomendamos: ${film}")
    }
  }

}
