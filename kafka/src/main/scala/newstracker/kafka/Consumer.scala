package newstracker.kafka

import cats.effect.kernel._
import cats.implicits._
import fs2.kafka._
import io.circe.Decoder
import io.circe.jawn.decodeByteArray

object Consumer {
  def make[F[_]: Async, K, V](
      config: KafkaConfig,
      autocommit: Boolean = false
  )(implicit kdecoder: Decoder[K], vdecoder: Decoder[V]): Resource[F, KafkaConsumer[F, K, V]] = {
    implicit val vd: Deserializer[F, V] = Deserializer.lift[F, V](decodeByteArray[V](_).liftTo[F])
    implicit val kd: Deserializer[F, K] = Deserializer.lift[F, K] { bytes =>
      val toDecode = if (bytes == null) "null".getBytes else bytes
      decodeByteArray[K](toDecode).liftTo[F]
    }

    val settings = ConsumerSettings[F, K, V]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(config.servers)
      .withGroupId(config.groupId)
      .withEnableAutoCommit(autocommit)

    KafkaConsumer.resource(settings)
  }
}
