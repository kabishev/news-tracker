import com.typesafe.sbt.packager.docker._
import Dependencies._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.kabishev"

val noPublish = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
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
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(docker)
  .settings(
    name                 := "news-tracker-core",
    moduleName           := "news-tracker-core",
    Docker / packageName := "news-tracker-core",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Dependencies.core ++ Dependencies.test
  )

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "news-tracker"
  )
  .aggregate(core)
