import sbt._
import sbt.Keys._
import play.Project._
//import org.ensime.sbt.Plugin.Settings.ensimeConfig
//import org.ensime.sbt.util.SExp._

object ApplicationBuild extends Build {

    val appName         = "bbweb"
    val appVersion      = "0.1-SNAPSHOT"

    val appDependencies = Seq(
      jdbc,
      cache,
      "ws.securesocial" %% "securesocial" % "2.1.3",
      "com.typesafe.play" %% "play-slick" % "0.6.0.1"
    )

  val main = Project(appName, file(".")).addPlugins(play.PlayScala).settings(

    version := appVersion,

    autoScalaLibrary := false,

    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",

    libraryDependencies ++= Seq(
      "ws.securesocial" %% "securesocial" % "2.1.3",
      "com.typesafe.akka" % "akka-persistence-experimental_2.10" % "2.3.0",
      "org.scala-stm" %% "scala-stm" % "0.7"  % "compile",
      "org.scalaz" %% "scalaz-core" % "7.0.6"  % "compile",
      "junit" % "junit" % "4.11" % "test",
      "org.scalatest" %% "scalatest" % "2.1.2" % "test",
      //"com.typesafe.akka" %% "akka-testkit" % "2.3.1" % "test",
      "com.github.ddevore" %% "akka-persistence-mongo-casbah"  % "0.6-SNAPSHOT" % "compile"
    ),

    scalacOptions ++= Seq(
      "deprecation",
      "-feature",
      "-language:implicitConversions",
      "-language:higherKinds",
      "-language:existentials",
      "-language:postfixOps",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-unchecked"),

    javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m"),

    javaOptions in Test += "-Dconfig.file=conf/test.conf",

    // in play 2.1.1 tests are run twice unless this option is defined
    testOptions += Tests.Argument(TestFrameworks.JUnit, "--ignore-runners=org.scalatest.junit.JUnitRunner")

    //templatesImport += "org.biobank.controllers._"

    //ensimeConfig := sexp(
    //    key(":only-include-in-index"), sexp(
    //      "controllers\\..*",
    //      "models\\..*",
    //      "views\\..*",
    //      "play\\..*"
    //    ),
    //    key(":source-roots"), sexp(
    //      "/home/loyola/proj/scala/playframework/Play20/framework/src"
    //    )
    //  )
  )
}
