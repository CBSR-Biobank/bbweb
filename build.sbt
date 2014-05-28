import PlayKeys._

name := """bbweb"""

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

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

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test ++=  Seq(
  "-Dconfig.file=conf/test.conf",
  "-Dlogger.resource=logback-test.xml"
)

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/report")

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.akka.io/snapshots/"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-persistence-experimental"  % "2.3.2"              % "compile",
  "com.typesafe.akka"      %% "akka-slf4j"                     % "2.3.2"              % "compile",
  "org.scala-stm"          %% "scala-stm"                      % "0.7"                % "compile",
  "org.scalaz"             %% "scalaz-core"                    % "7.0.6"              % "compile",
  "org.scalaz"             %% "scalaz-typelevel"               % "7.0.6"              % "compile",
  "org.webjars"            %  "requirejs"                      % "2.1.11"             % "compile",
  "org.webjars"            %  "webjars-locator"                % "0.13",
  "org.webjars"            %% "webjars-play"                   % "2.3.0-RC1",
  "org.webjars"            %  "underscorejs"                   % "1.6.0-1",
  "org.webjars"            %  "jquery"                         % "1.11.0-1",
  "org.webjars"            %  "angularjs"                      % "1.2.16" exclude(
    "org.webjars", "jquery"),
  "org.webjars"            %  "bootstrap"                      % "3.1.1" exclude(
    "org.webjars", "jquery"),
  "org.scalatest"          %% "scalatest"                      % "2.1.5"              % "test->*" excludeAll(
    ExclusionRule(organization = "org.junit", name = "junit")
  ),
  //"com.typesafe.akka"    %% "akka-testkit"                   % "2.3.1"              % "test",
  "com.github.ddevore"     %% "akka-persistence-mongo-casbah"  % "0.7.2-SNAPSHOT"     % "compile",
  //"se.radley"              %% "play-plugins-enumeration"       % "1.1.0"              % "compile",
  "com.typesafe"           %% "webdriver"                      % "1.0.0-M2"           % "test",
  "com.typesafe.akka"      %% "akka-testkit"                   % "2.3.2"              % "test"
  //"com.typesafe.play"    %% "play-slick"                     % "0.6.0.1",
)

//dependencyOverrides += "com.typesafe.akka" %% "akka-actor" % "2.3.2"

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

lazy val root = (project in file(".")).enablePlugins(PlayScala)

net.virtualvoid.sbt.graph.Plugin.graphSettings

// This tells Play to optimize this file and its dependencies
requireJs += "main.js"

// The main config file
// See http://requirejs.org/docs/optimization.html#mainConfigFile
requireJsShim := "build.js"


// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")
