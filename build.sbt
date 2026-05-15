name := "scala-parking-payment-service"

version := "1.0.0"

scalaVersion := "2.13.18"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    libraryDependencies ++= Seq(
      // Play Framework
      guice,

      // Database
      "com.typesafe.play" %% "play-slick" % "5.1.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.1.0",
      "mysql" % "mysql-connector-java" % "8.0.33",

      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % Test,
      "org.mockito" %% "mockito-scala" % "1.17.12" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Wconf:cat=unused-imports&site=<.*>:s"
    )
  )
