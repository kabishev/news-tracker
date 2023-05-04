package newstracker.kafka

import cats.effect.kernel._
import cats.implicits._
import fs2._
import fs2.kafka._
import io.circe.Encoder

trait Producer[F[_], K, V] {
  def pipe: Pipe[F, (K, V), Unit]
}

final private class LiveProducer[F[_]: Async, K, V](
    config: KafkaConfig,
    topic: String
)(implicit kencoder: Encoder[K], vencoder: Encoder[V])
    extends Producer[F, K, V] {

  implicit val ks: Serializer[F, K] = Serializer.lift[F, K](kencoder(_).noSpaces.getBytes.pure[F])
  implicit val vs: Serializer[F, V] = Serializer.lift[F, V](vencoder(_).noSpaces.getBytes.pure[F])

  private val settings: ProducerSettings[F, K, V] =
    ProducerSettings(keySerializer = ks, valueSerializer = vs)
      .withBootstrapServers(config.servers)

  override def pipe: Pipe[F, (K, V), Unit] =
    _.map { case (key, value) => ProducerRecord(topic, key, value) }
      .map(rec => ProducerRecords.one(rec))
      .through(KafkaProducer.pipe(settings))
      .drain
}

object Producer {
  def make[F[_]: Async, K, V](
      config: KafkaConfig,
      topic: String
  )(implicit kencoder: Encoder[K], vencoder: Encoder[V]): Resource[F, Producer[F, K, V]] =
    Resource.eval(Async[F].delay(new LiveProducer[F, K, V](config, topic)))
}
