package newstracker.health

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._

import newstracker.ControllerSpec

import java.time.Instant

class HealthControllerSpec extends ControllerSpec {
  val ts = Instant.parse("2020-02-05T00:00:00Z")

  "A HealthController" should {
    "return status" in {
      val controller = IO(new HealthController[IO](ts))

      val request  = Request[IO](uri = uri"/health/status", method = Method.GET)
      val response = controller.flatMap(_.routes(null).orNotFound.run(request))

      response.mustHaveStatus(Status.Ok, Some(s"""{"startupTime":"$ts"}"""))
    }
  }
}
