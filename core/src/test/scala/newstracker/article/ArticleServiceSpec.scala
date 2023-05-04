package newstracker.article

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar

import newstracker.article.ArticleFixtures
import newstracker.article.db.ArticleRepository
import newstracker.article.domain.{Article, CreateArticle}

class ArticleServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar {
  "A ArticleService" should {
    "create new article" in {
      val repo = mock[ArticleRepository[IO]]
      when(repo.create(any[CreateArticle])).thenReturn(IO.pure(ArticleFixtures.aid))

      val actual = for {
        svc <- ArticleService.make[IO](repo)
        res <- svc.create(ArticleFixtures.create())
      } yield res

      actual.unsafeToFuture().map { a =>
        verify(repo).create(ArticleFixtures.create())
        a mustBe ArticleFixtures.aid
      }
    }

    "get article" in {
      val repo = mock[ArticleRepository[IO]]
      when(repo.get(ArticleFixtures.aid)).thenReturn(IO.pure(ArticleFixtures.article()))

      val actual = for {
        svc <- ArticleService.make[IO](repo)
        res <- svc.get(ArticleFixtures.aid)
      } yield res

      actual.unsafeToFuture().map { a =>
        verify(repo).get(ArticleFixtures.aid)
        a mustBe ArticleFixtures.article()
      }
    }

    "get all articles" in {
      val repo = mock[ArticleRepository[IO]]
      when(repo.getAll).thenReturn(Stream(ArticleFixtures.article()))

      val actual = for {
        svc <- ArticleService.make[IO](repo)
        res <- svc.getAll.compile.toList
      } yield res

      actual.unsafeToFuture().map { a =>
        verify(repo).getAll
        a mustBe List(ArticleFixtures.article())
      }
    }

    "update article" in {
      val repo = mock[ArticleRepository[IO]]
      when(repo.update(any[Article])).thenReturn(IO.unit)

      val actual = for {
        svc <- ArticleService.make[IO](repo)
        res <- svc.update(ArticleFixtures.article())
      } yield res

      actual.unsafeToFuture().map { a =>
        verify(repo).update(ArticleFixtures.article())
        a mustBe ()
      }
    }
  }
}
