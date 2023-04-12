import sbt._

object Dependencies {
  object Versions {
    val circe          = "0.14.5"
    val http4s         = "0.23.18"
    val log4cats       = "2.5.0"
    val pureConfig     = "0.17.2"
    val fs2            = "3.6.1"
    val kafka          = "2.5.0"
    val confluent      = "5.5.0"
    val newtype        = "0.4.4"
    val contextApplied = "0.1.4"
    val mongo4cats     = "0.6.10"
    val cats           = "2.9.0"
    val catsEffect     = "3.4.8"
    val slf4j          = "2.0.7"
    val scalatest      = "3.2.15"
    val scalacheck     = "1.17.0"
    val tapir          = "1.2.12"
    val organizeImports = "0.6.0"
  }

  object Libraries {
    val newtype = "io.estatico" %% "newtype" % Versions.newtype
    val organizeImports = "com.github.liancheng" %% "organize-imports" % Versions.organizeImports

    object cats {
      val core          = "org.typelevel"  %% "cats-core"      % Versions.cats
      val effect        = "org.typelevel"  %% "cats-effect"    % Versions.catsEffect
      val kindProjector = ("org.typelevel" %% "kind-projector" % "0.13.2").cross(CrossVersion.full)
      val all           = Seq(core, effect, kindProjector)
    }

    object fs2 {
      val core  = "co.fs2"          %% "fs2-core"  % Versions.fs2
      val kafka = "com.github.fd4s" %% "fs2-kafka" % Versions.kafka
      val all   = Seq(core, kafka)
    }

    object kafka {
      val clients = "org.apache.kafka"  % "kafka-clients"         % Versions.kafka
      val streams = "org.apache.kafka" %% "kafka-streams-scala"   % Versions.kafka
      val avro    = "io.confluent"      % "kafka-avro-serializer" % Versions.confluent
      val all     = Seq(clients, streams, avro)
    }

    object circe {
      val core    = "io.circe" %% "circe-core"    % Versions.circe
      val parser  = "io.circe" %% "circe-parser"  % Versions.circe
      val generic = "io.circe" %% "circe-generic" % Versions.circe
      val all     = Seq(core, parser, generic)
    }

    object http4s {
      val emberServer = "org.http4s" %% "http4s-ember-server" % Versions.http4s
      val circe       = "org.http4s" %% "http4s-circe"        % Versions.http4s
      val dsl         = "org.http4s" %% "http4s-dsl"          % Versions.http4s
      val all         = Seq(emberServer, circe, dsl)
    }

    object mongo4cats {
      val core     = "io.github.kirill5k" %% "mongo4cats-core"     % Versions.mongo4cats
      val circe    = "io.github.kirill5k" %% "mongo4cats-circe"    % Versions.mongo4cats
      val embedded = "io.github.kirill5k" %% "mongo4cats-embedded" % Versions.mongo4cats
    }

    object tapir {
      val core   = "com.softwaremill.sttp.tapir" %% "tapir-core"          % Versions.tapir
      val circe  = "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % Versions.tapir
      val http4s = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Versions.tapir
      val all    = Seq(core, circe, http4s)
    }

    object pureConfig {
      val core = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    }

    object logging {
      val slf4j         = "org.slf4j"      % "slf4j-simple"   % Versions.slf4j
      val log4cats      = "org.typelevel" %% "log4cats-core"  % Versions.log4cats
      val log4catsSlf4j = "org.typelevel" %% "log4cats-slf4j" % Versions.log4cats
      val all           = Seq(slf4j, log4cats, log4catsSlf4j)
    }

    object testing {
      val scalatest     = "org.scalatest"           %% "scalatest"      % Versions.scalatest
      val scalacheck    = "org.scalacheck"          %% "scalacheck"     % Versions.scalacheck
      val kafkaEmbedded = "io.github.embeddedkafka" %% "embedded-kafka" % Versions.kafka
    }
  }

  lazy val core =
    Libraries.cats.all ++
      Libraries.http4s.all ++
      Libraries.circe.all ++
      Libraries.tapir.all ++
      Libraries.logging.all ++
      Seq(
        Libraries.fs2.core,
        Libraries.pureConfig.core,
        Libraries.newtype,
        Libraries.mongo4cats.core,
        Libraries.mongo4cats.circe
      )
  lazy val testCore = Seq(
    Libraries.testing.scalatest,
    Libraries.testing.scalacheck
  ).map(_ % Test)

  lazy val kafka =
    Libraries.cats.all ++
      Libraries.kafka.all ++
      Libraries.fs2.all ++
      Libraries.circe.all ++
      Libraries.logging.all

  lazy val testKafka = Seq(
    Libraries.testing.scalatest,
    Libraries.testing.kafkaEmbedded
  ).map(_ % Test)

  lazy val scalafix = Seq(
    Libraries.organizeImports
  )
}
