package newstracker.kafka

import cats.effect.kernel._
import cats.implicits._
import fs2.kafka._
import io.circe.Decoder
import io.circe.jawn.decodeByteArray

object Consumer {
  def makeWithoutKey[F[_]: Async, V](config: KafkaConfig)(implicit vdecoder: Decoder[V]): Resource[F, KafkaConsumer[F, Unit, V]] = {
    implicit val vd: Deserializer[F, V] = Deserializer.lift[F, V](decodeByteArray[V](_).liftTo[F])
    implicit val kd: Deserializer[F, Unit] = Deserializer.lift[F, Unit](_ => ().pure[F])

    val settings = ConsumerSettings[F, Unit, V]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers(config.servers)
      .withGroupId(config.groupId)
      .withEnableAutoCommit(true)  // do you always need autocommit=true?

    KafkaConsumer.resource(settings)
  }

  // you may make it more universal like that:
  //  def make[F[_]: Async, K: Deserializer[F, *], V](config: KafkaConfig, autocommit = false)(implicit vdecoder: Decoder[V]): Resource[F, KafkaConsumer[F, K, V]]
  //  and specify settings for manual commit
}
