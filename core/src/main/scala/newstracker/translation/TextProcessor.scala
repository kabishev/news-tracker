package newstracker.translation

import cats.effect._
import org.jsoup.Jsoup
import org.jsoup.nodes._

trait TextProcessor[F[_]] {
  def preprocessing(text: String): F[String]
  def postprocessing(text: String, translated: String): F[String]
}

object TextProcessor {
  def makeJsoupProcessor[F[_]: Async]: TextProcessor[F] = new TextProcessor[F] {
    override def preprocessing(text: String): F[String] = Async[F].delay {
      Jsoup.parse(text).select("div.caas-body").html()
    }

    override def postprocessing(text: String, translated: String): F[String] = Async[F].delay {
      val document: Document = Jsoup.parse(text)
      val outputSettings = document
        .outputSettings()
        .clone()
        .indentAmount(0)
        .prettyPrint(false)
        .escapeMode(Entities.EscapeMode.xhtml)

      document.outputSettings(outputSettings)

      val caasNode = document.select("div.caas-body").html(translated)
      document.select("div.caas-body").first().replaceWith(caasNode.first())

      document.html()
    }
  }
}
