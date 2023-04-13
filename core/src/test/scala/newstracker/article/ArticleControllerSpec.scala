package newstracker.article

import cats.effect.IO
import org.http4s.implicits._
import org.http4s.{Method, Status, Uri}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._

import newstracker.ControllerSpec
import newstracker.article.domain._
import newstracker.fixtures.Articles

import java.time.LocalDate

class ArticleControllerSpec extends ControllerSpec {
  "A ArticleController" when {

    "POST /articles" should {
      "create new article and return 201 on success" in {
        val svc = mock[ArticleService[IO]]
        when(svc.create(any[CreateArticle])).thenReturn(IO.pure(Articles.aid))

        val req = request(
          uri"/articles",
          method = Method.POST,
          body = Some(
            parseJson(s"""{"title": "${Articles.title}","content": "content","createdAt": "${Articles.createdAt}","language": "en"}""")
          )
        )
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        res mustHaveStatus (Status.Created, Some(s"""{"id":"${Articles.aid}"}"""))
        verify(svc).create(Articles.create())
      }
    }

    "GET /articles" should {
      "return articles" in {
        val svc = mock[ArticleService[IO]]
        when(svc.getAll).thenReturn(IO.pure(List(Articles.article())))

        val req = request(uri"/articles", method = Method.GET)
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        val expected =
          s"""[{"id":"${Articles.aid}","title":"${Articles.title}","content":"content","createdAt":"${Articles.createdAt}","language":"en","tags":[]}]"""

        res mustHaveStatus (Status.Ok, Some(expected))
        verify(svc).getAll
      }
    }

    "GET /articles/:id" should {
      "return article by id" in {
        val svc = mock[ArticleService[IO]]
        when(svc.get(any[ArticleId])).thenReturn(IO.pure(Articles.article()))
        when(svc.isValidId(any[String])).thenReturn(true)

        val req = request(Uri.unsafeFromString(s"/articles/${Articles.aid}"), method = Method.GET)
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        val expected =
          s"""{"id":"${Articles.aid}","title":"${Articles.title}","content":"content","createdAt":"${Articles.createdAt}","language":"en","tags":[]}"""
        res mustHaveStatus (Status.Ok, Some(expected))
        verify(svc).get(Articles.aid)
      }

      "return error when article id is invalid" in {
        val svc = mock[ArticleService[IO]]
        when(svc.isValidId("invalid")).thenReturn(false)

        val req = request(Uri.unsafeFromString(s"/articles/invalid"), method = Method.GET)
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        res mustHaveStatus (Status.UnprocessableEntity, Some("""{"message":"Invalid representation of an id: invalid"}"""))
        verify(svc).isValidId("invalid")
        verifyNoMoreInteractions(svc);
      }
    }

    "PUT /articles/:id" should {
      "update article" in {
        val svc = mock[ArticleService[IO]]
        when(svc.update(any[Article])).thenReturn(IO.unit)
        when(svc.isValidId(any[String])).thenReturn(true)

        val req = request(
          Uri.unsafeFromString(s"/articles/${Articles.aid}"),
          method = Method.PUT,
          body = Some(
            parseJson(s"""{"title":"${Articles.title}","content":"content","createdAt":"${Articles.createdAt}","language":"en"}""")
          )
        )
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        res mustHaveStatus (Status.NoContent, None)
        verify(svc).update(Articles.article())
      }

      "return 422 when request has validation errors" in {
        val svc = mock[ArticleService[IO]]
        when(svc.isValidId(any[String])).thenReturn(true)

        val req = request(
          Uri.unsafeFromString(s"/articles/${Articles.aid}"),
          method = Method.PUT,
          body = Some(parseJson(s"""{"title":"","content":""}"""))
        )
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        val expected =
          """{"message":"Field title cannot be empty, Field content cannot be empty, Missing required field createdAt, Missing required field language"}"""

        res mustHaveStatus (Status.UnprocessableEntity, Some(expected))
        verify(svc, times(2)).isValidId(any[String])
        verifyNoMoreInteractions(svc);
      }

      "return 404 when article does not exist" in {
        val svc = mock[ArticleService[IO]]
        when(svc.update(any[Article])).thenReturn(IO.raiseError(errors.ArticleDoesNotExist(Articles.aid)))
        when(svc.isValidId(any[String])).thenReturn(true)

        val req = request(
          Uri.unsafeFromString(s"/articles/${Articles.aid}"),
          method = Method.PUT,
          body =
            Some(parseJson(s"""{"title":"${Articles.title}","content":"content","createdAt":"${Articles.createdAt}","language":"en"}"""))
        )
        val res = ArticleController.make[IO](svc).flatMap(_.routes.orNotFound.run(req))

        val resBody = s"""{"message":"Article with id ${Articles.aid} does not exist"}"""
        res mustHaveStatus (Status.NotFound, Some(resBody))
        verify(svc).update(Articles.article())
      }
    }
  }
}
