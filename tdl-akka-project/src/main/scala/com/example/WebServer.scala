package webserver 

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import commons.{EntryManager, RecommendationRequest, WebClient}

import akka.http.scaladsl.marshalling._


class WebServer(val entryManager: ActorRef)(implicit val actorSystem: ActorSystem) extends HttpApp {

  val resourcePrefix = "resources"

  def routes() =
      pathSingleSlash {
        concat(
          get {
            getFromResource("index/index.html")
          },
          post {
                formFields("query") { value  =>
                  println(s"Buscando pelicula $value")
                  completeWith(implicitly[ToResponseMarshaller[HttpEntity.Strict]]) { f =>
                    entryManager ! RecommendationRequest(value, WebClient(f)) 
                  }
                }
          }
        )
      } ~ 
      path(resourcePrefix / Remaining) { resource =>
        // Ruta necesaria para los .css y .png
        get{
          getFromResource("index/" + resource)
        }
      }
}
