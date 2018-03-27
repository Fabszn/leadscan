import java.time.{LocalDate, LocalTime}

name := """devoxx-leadtracker"""

version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.9"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, BuildInfoPlugin)
  .settings(
    executableScriptName := (name in Universal).value,
    packageName in Universal <<= (name in Universal, version) { (n, v) => s"$n-$v" },
    buildInfoKeys := Seq[BuildInfoKey](name in Universal, version, scalaVersion, sbtVersion,
      BuildInfoKey.action("BuildDate") {
        LocalDate.now() + " - " + LocalTime.now()
      }),
    buildInfoOptions += BuildInfoOption.ToJson,
    libraryDependencies ++= Seq(
      jdbc,
      "org.flywaydb" %% "flyway-play" % "3.0.1",
      "org.mockito" % "mockito-core" % "2.7.2",
      "com.typesafe.play" %% "anorm" % "2.5.3",
      "org.postgresql" % "postgresql" % "9.4.1212",
      "io.github.scala-hamsters" %% "hamsters" % "1.1.1",
      "com.github.pathikrit" %% "better-files" % "2.17.1",
      cache,
      ws,
      filters,
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
      "com.pauldijou" %% "jwt-core" % "0.9.2",
      "com.typesafe.play" %% "play-mailer" % "5.0.0",
      "com.opencsv" % "opencsv" % "3.8",
      "org.apache.commons" % "commons-lang3" % "3.1",
      "org.specs2" %% "specs2-core" % "3.7.3" % "test"
    ))
