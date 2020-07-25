package commons

abstract class Client
abstract class Request
case class MyTweet(mentionId: Long, user: String) extends Client
