import Dependencies._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.kabishev"

val noPublish = Seq(
  publish         := {},
  publishLocal    := {},
  publishArtifact := false,
  publish / skip  := true
)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "news-tracker-core",
    moduleName := "news-tracker-core",
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
