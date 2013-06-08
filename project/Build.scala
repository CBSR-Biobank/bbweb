import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "bbweb"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
    )

  val main = play.Project(
    appName, appVersion, appDependencies
  ).settings(

    resolvers ++= Seq(
      "Eligosource Releases Repo" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases/"
    ),

    libraryDependencies ++= Seq(
      "org.eligosource" %% "eventsourced-core" % "0.5.0",
      "org.eligosource" %% "eventsourced-journal-mongodb-casbah" % "0.5.0",
      "org.scala-stm" %% "scala-stm" % "0.7" % "compile",
      "org.scalaz" %% "scalaz-core" % "6.0.4" % "compile",

      "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test",
      "junit" % "junit" % "4.11" % "test",
      "org.typelevel" %% "scalaz6-specs2" % "0.1" % "test"
    ),

    scalacOptions ++= Seq("-feature"),
    
    lessEntryPoints <<= baseDirectory(customLessEntryPoints)
  )
  
  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory 
  def customLessEntryPoints(base: File): PathFinder = ( 
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less") 
    +++ (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less") 
    +++ (base / "app" / "assets" / "stylesheets" * "*.less") )
}
