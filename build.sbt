name := "bbweb"

version := "0.1-SNAPSHOT"

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
  "-unchecked")

javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test += "-Dconfig.file=conf/test.conf"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "org.webjars" % "angularjs" % "1.2.16",
  "org.webjars" % "requirejs" % "2.1.11",
  "org.webjars" % "webjars-play_2.10" % "2.3-M1",
  "org.webjars" % "bootstrap" % "3.1.1",
  "com.typesafe" %% "webdriver" % "1.0.0-M2",
  "com.typesafe" %% "jse" % "1.0.0-M2",
  "com.typesafe" %% "npm" % "1.0.0-M2",
  "org.scala-stm" %% "scala-stm" % "0.7"  % "compile",
  "org.scalaz" %% "scalaz-core" % "7.0.6"  % "compile",
  "org.scalatest" %% "scalatest" % "2.1.2" % "test",
  "ws.securesocial" %% "securesocial" % "play-2.3-SNAPSHOT",
  //"com.typesafe.akka" %% "akka-testkit" % "2.3.1" % "test",
  //"com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "com.github.ddevore" %% "akka-persistence-mongo-casbah"  % "0.6-SNAPSHOT" % "compile"
)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

lazy val root = (project in file(".")).addPlugins(PlayScala)
