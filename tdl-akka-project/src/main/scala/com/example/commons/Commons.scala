package commons

import akka.actor.{ActorRef}
import webserver.WebServer

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }

abstract class Client
abstract class Request

case class MyTweet(mentionId: Long, user: String) extends Client
case class WebClient(complete: HttpEntity.Strict => Unit) extends Client




