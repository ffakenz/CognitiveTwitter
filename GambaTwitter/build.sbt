import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "GambaTwitter"
      , scalaVersion := "2.12.3"
      , version      := "0.1.0-SNAPSHOT"
      , resolvers += "Maven central" at "http://repo1.maven.org/maven2/"
    )),
    name := "Gamba",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion
      , "com.danielasfregola" %% "twitter4s" % twitter4sVersion
      , "com.ibm.watson.developer_cloud" % "java-sdk" % watsonVersion
      // Test Enviroment
      , "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
      , "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )