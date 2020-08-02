package commons

import akka.actor.{ActorRef}

abstract class Client
abstract class Request

case class MyTweet(mentionId: Long, user: String) extends Client
case class WebClient(returnTo: ActorRef) extends Client
