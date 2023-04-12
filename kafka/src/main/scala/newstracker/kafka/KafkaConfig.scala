package newstracker.kafka

final case class KafkaConfig(
    servers: String,
    groupId: String
)
