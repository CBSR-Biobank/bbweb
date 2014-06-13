import PlayKeys._

name := """bbweb"""

organization := "org.biobank"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalacOptions ++= Seq(
  "-target:jvm-1.7",
  "-encoding", "UTF-8",
  "deprecation",        // warning and location for usages of deprecated APIs
  "-feature",           // warning and location for usages of features that should be imported explicitly
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-unchecked",          // additional warnings where generated code depends on assumptions
  "-Xlint",
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
)

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
  cache,
  filters,
  "com.typesafe.akka"      %% "akka-persistence-experimental"  % "2.3.2"              % "compile",
  "com.typesafe.akka"      %% "akka-slf4j"                     % "2.3.2"              % "compile",
  "org.scala-stm"          %% "scala-stm"                      % "0.7"                % "compile",
  "org.scalaz"             %% "scalaz-core"                    % "7.0.6"              % "compile",
  "org.scalaz"             %% "scalaz-typelevel"               % "7.0.6"              % "compile",
  // WebJars infrastructure
  "org.webjars"            %  "webjars-locator"                % "0.13",
  "org.webjars"            %% "webjars-play"                   % "2.3.0",
  // WebJars dependencies
  "org.webjars"            % "requirejs"                       % "2.1.11-1",
  "org.webjars"            %  "underscorejs"                   % "1.6.0-3",
  "org.webjars"            % "jasmine"                         % "2.0.0",
  "org.webjars"            %  "jquery"                         % "2.1.1",
  "org.webjars"            %  "bootstrap"                      % "3.1.1-1" exclude(
    "org.webjars", "jquery"),
  "org.webjars"            %  "angularjs"                      % "1.3.0-beta.8" exclude(
    "org.webjars", "jquery"),
  "org.webjars"            %  "angular-ui-bootstrap"           % "0.11.0-2",
  "org.webjars"            %  "angular-ui-router"              % "0.2.10-1",
  "org.webjars"            %  "ng-table"                       % "0.3.2",
  "org.scalatest"          %% "scalatest"                      % "2.1.5"              % "test->*" excludeAll(
    ExclusionRule(organization = "org.junit", name = "junit")
  ),
  "com.github.ddevore"     %% "akka-persistence-mongo-casbah"  % "0.7.2-SNAPSHOT"     % "compile",
  // Test Dependencies
  "com.typesafe.akka"      %% "akka-testkit"                   % "2.3.2"              % "test",
)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

net.virtualvoid.sbt.graph.Plugin.graphSettings

//MochaKeys.requires += "./setup.js"

// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// RequireJS with sbt-rjs (https://github.com/sbt/sbt-rjs#sbt-rjs)
// ~~~
RjsKeys.paths += ("jsRoutes" -> ("/jsroutes" -> "empty:"))

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")
