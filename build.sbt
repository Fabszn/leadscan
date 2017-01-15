name := """devoxx-leadtracker"""

version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ++= Seq(
    jdbc,
    "com.typesafe.play" %% "anorm" % "2.5.1",
    "org.postgresql" % "postgresql" % "9.4.1212",
    cache,
    ws,
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
  ))
