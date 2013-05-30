import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "bbweb"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      "be.objectify"  %%  "deadbolt-java"     % "2.1-SNAPSHOT",
      "com.feth"      %%  "play-authenticate" % "0.2.5-SNAPSHOT"
    )

  val main = play.Project(
    appName, appVersion, appDependencies
  ).settings(

    resolvers ++= Seq(
      "Eligosource Releases Repo" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases/"
    ),

    resolvers += Resolver.url(
      "Objectify Play Repository (release)",
      url("http://schaloner.github.com/releases/")
      )(Resolver.ivyStylePatterns),

    resolvers += Resolver.url(
      "Objectify Play Repository (snapshot)",
      url("http://schaloner.github.com/snapshots/")
    )(Resolver.ivyStylePatterns),

    resolvers += Resolver.url(
      "play-easymail (release)",
      url("http://joscha.github.com/play-easymail/repo/releases/")
    )(Resolver.ivyStylePatterns),

    resolvers += Resolver.url(
      "play-easymail (snapshot)",
      url("http://joscha.github.com/play-easymail/repo/snapshots/")
    )(Resolver.ivyStylePatterns),

    resolvers += Resolver.url(
      "play-authenticate (release)",
      url("http://joscha.github.com/play-authenticate/repo/releases/")
    )(Resolver.ivyStylePatterns),

    resolvers += Resolver.url(
      "play-authenticate (snapshot)",
      url("http://joscha.github.com/play-authenticate/repo/snapshots/")
    )(Resolver.ivyStylePatterns),

    libraryDependencies ++= Seq(
      "org.eligosource" %% "eventsourced-core" % "0.5.0",
      "org.eligosource" %% "eventsourced-journal-mongodb-casbah" % "0.5.0",
      "org.scala-stm" %% "scala-stm" % "0.7" % "compile",
      "org.scalaz" %% "scalaz-core" % "6.0.4" % "compile",

      "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test",
      "junit" % "junit" % "4.11" % "test",
      "org.typelevel" %% "scalaz6-specs2" % "0.1" % "test"
    ),

    scalacOptions ++= Seq("-feature")
  )
}
