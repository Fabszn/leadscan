import java.time.{LocalDate, LocalTime}

name := """devoxx-leadtracker"""

version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

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
      "com.typesafe.play" %% "anorm" % "2.5.1",
      "org.postgresql" % "postgresql" % "9.4.1212",
      cache,
      ws,
      filters,
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test

    ))
