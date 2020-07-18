import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.HttpApp
import akka.http.scaladsl.server.Route
 
class WebServer extends HttpApp {

  override def routes: Route =
    concat(
      pathSingleSlash {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1> This is not an index <h1>"))
        }
      },
      path("test") {
        get {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h2> Hello world! </h2>"))
        }
      }
    )
  }
