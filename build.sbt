import com.typesafe.sbt.packager.docker._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.kabishev"
ThisBuild / scalafixDependencies ++= Dependencies.scalafix

val noPublish = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

val common = Seq(
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-feature",
    "-deprecation",
    "-language:implicitConversions"
  )
)

val docker = Seq(
  packageName        := moduleName.value,
  version            := version.value,
  dockerBaseImage    := "adoptopenjdk/openjdk11:jre-11.0.18_10-alpine",
  dockerUpdateLatest := true,
  dockerUsername     := sys.env.get("DOCKER_USERNAME"),
  dockerRepository   := sys.env.get("DOCKER_REPO_URI"),
  makeBatScripts     := Nil,
  dockerCommands := {
    val commands         = dockerCommands.value
    val (stage0, stage1) = commands.span(_ != DockerStageBreak)
    val (before, after)  = stage1.splitAt(4)
    val installBash      = Cmd("RUN", "apk update && apk upgrade && apk add bash")
    stage0 ++ before ++ List(installBash) ++ after
  }
)

lazy val core = project
  .in(file("core"))
  .dependsOn(kafka)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)
  .settings(docker)
  .settings(
    name                 := "news-tracker-core",
    moduleName           := "news-tracker-core",
    Docker / packageName := "news-tracker-core",
    libraryDependencies ++= Dependencies.core ++ Dependencies.testCore
  )

lazy val kafka = project
  .in(file("kafka"))
  .settings(common)
  .settings(
    name       := "news-tracker-kafka",
    moduleName := "news-tracker-kafka",
    resolvers ++= Seq(
      "io.confluent".at("https://packages.confluent.io/maven/")
    ),
    libraryDependencies ++= Dependencies.kafka ++ Dependencies.testKafka
  )

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "news-tracker"
  )
  .aggregate(core)
