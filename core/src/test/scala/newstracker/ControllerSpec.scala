package newstracker

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import io.circe.parser._
import io.circe.Json
import org.http4s._
import org.scalatest.Assertion
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

class ControllerSpec extends AnyWordSpec with MockitoSugar with Matchers {
  def request(
      uri: org.http4s.Uri,
      method: org.http4s.Method = Method.GET,
      body: Option[Json] = None
  ): Request[IO] = Request[IO](
    uri = uri,
    method = method,
    body = body.map(b => Stream.emits(b.noSpaces.getBytes().toList)).getOrElse(EmptyBody)
  )

  implicit class ResponceOps(response: IO[Response[IO]]) {
    def mustHaveStatus(expectedStatus: Status, expectedBody: Option[String] = None): Assertion =
      response
        .flatTap(res => IO(res.status mustBe expectedStatus))
        .flatMap { res =>
          expectedBody match {
            case Some(expectedJson) => res.as[String].map(parse(_) mustBe parse(expectedJson))
            case None               => res.body.compile.toVector.map(_ mustBe empty)
          }
        }
        .unsafeRunSync()
  }

  def parseJson(jsonString: String): Json = parse(jsonString).getOrElse(throw new RuntimeException)
}
