import sbt._
import sbt.Keys._
import play.Project._
import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._

object ApplicationBuild extends Build {

    val appName         = "bbweb"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      "securesocial" %% "securesocial" % "master-SNAPSHOT",

      // in play 2.1.1 tests are run twice unless dependency is added
      "com.novocode" % "junit-interface" % "0.10-M2"
    )

  val main = play.Project(
    appName, appVersion, appDependencies).settings(
    scalaVersion := "2.10.2",

    autoScalaLibrary := false,

    resolvers ++= Seq(
      "Eligosource Releases Repo" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases/"

      //Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)

    ),

    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns),

    libraryDependencies ++= Seq(
      "org.eligosource" %% "eventsourced-core" % "0.5.0",
      "org.eligosource" %% "eventsourced-journal-mongodb-casbah" % "0.5.0",
      "org.scala-stm" %% "scala-stm" % "0.7" % "compile",
      "org.scalaz" %% "scalaz-core" % "6.0.4" % "compile",

      "com.typesafe.akka" % "akka-testkit" % "2.0.3" % "test",
      "junit" % "junit" % "4.11" % "test",
      "org.typelevel" %% "scalaz6-specs2" % "0.1" % "test",
      "org.pegdown" % "pegdown" % "1.2.1" // specs2 html output
    ),

    scalacOptions ++= Seq("-feature"),

    (testOptions in Test) += Tests.Argument(TestFrameworks.Specs2, "html", "console"),

    // in play 2.1.1 tests are run twice unless this option is defined
    testOptions += Tests.Argument(TestFrameworks.JUnit, "--ignore-runners=org.specs2.runner.JUnitRunner"),

    lessEntryPoints <<= baseDirectory(customLessEntryPoints),

    ensimeConfig := sexp(
        key(":only-include-in-index"), sexp(
          "controllers\\..*",
          "models\\..*",
          "views\\..*",
          "play\\..*"
        ),
        key(":source-roots"), sexp(
          "/home/loyola/proj/scala/playframework/Play20/framework/src"
        )
      )
  )

  // Only compile the bootstrap bootstrap.less file and any other *.less file in the stylesheets directory
  def customLessEntryPoints(base: File): PathFinder = (
    (base / "app" / "assets" / "stylesheets" / "bootstrap" * "bootstrap.less")
    +++ (base / "app" / "assets" / "stylesheets" / "bootstrap" * "responsive.less")
    +++ (base / "app" / "assets" / "stylesheets" * "*.less") )
}
