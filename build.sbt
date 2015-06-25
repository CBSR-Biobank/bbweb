import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "bbweb"

organization in ThisBuild := "org.biobank"

version := "0.1-SNAPSHOT"

def excludeSpecs2(module: ModuleID): ModuleID =
  module.excludeAll(ExclusionRule(organization = "org.specs2", name = "specs2"))
    .exclude("com.novocode", "junit-interface")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(libraryDependencies ~= (_.map(excludeSpecs2)))

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.6")

scalacOptions in ThisBuild ++= Seq(
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
  "-Ywarn-unused-import",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
)

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

//javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test ++=  Seq(
  "-Dconfig.file=conf/test.conf",
  "-Dlogger.resource=logback-test.xml"
)

testOptions in Test := Nil

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/report")

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "Typesafe repository"          at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS"                 at "https://oss.sonatype.org/content/repositories/releases",
  "Akka Snapshots"               at "http://repo.akka.io/snapshots/"
)

libraryDependencies ++= Seq(
  cache,
  filters,
  "com.typesafe.akka"         %% "akka-persistence-experimental"     % "2.3.11"             % "compile" excludeAll(
    ExclusionRule(organization="com.google.protobuf")
  ),
  "com.typesafe.akka"         %% "akka-remote"                       % "2.3.11"             % "compile",
  "com.typesafe.akka"         %% "akka-slf4j"                        % "2.3.11"             % "compile",
  "org.scala-stm"             %% "scala-stm"                         % "0.7"                % "compile",
  "org.scalaz"                %% "scalaz-core"                       % "7.1.2"              % "compile",
  "com.github.ironfish"       %% "akka-persistence-mongo-casbah"     % "0.7.5"              % "compile",
  "com.github.t3hnar"         %% "scala-bcrypt"                      % "2.4",
  "com.typesafe.play"         %% "play-mailer"                       % "3.0.1",
  // WebJars infrastructure
  "org.webjars"               %% "webjars-play"                      % "2.4.0-1" exclude(
    "org.webjars", "requirejs"),
  // WebJars dependencies
  "org.webjars"               %  "requirejs"                         % "2.1.18",
  "org.webjars"               %  "underscorejs"                      % "1.8.3",
  "org.webjars"               %  "jquery"                            % "2.1.4",
  "org.webjars"               %  "bootstrap"                         % "3.3.4" excludeAll(
    ExclusionRule(organization="org.webjars")
  ),
  "org.webjars"               %  "angularjs"                         % "1.4.0" exclude(
    "org.webjars", "jquery"),
  "org.webjars"               %  "angular-ui-bootstrap"              % "0.13.0" exclude(
    "org.webjars", "angularjs"),
  "org.webjars"               %  "angular-ui-router"                 % "0.2.15" exclude(
    "org.webjars", "angularjs"),
  "org.webjars"               %  "ng-table"                          % "0.3.3",
  "org.webjars"               %  "toastr"                            % "2.1.0" exclude(
    "org.webjars", "jquery"),
  "org.webjars"               %  "angular-sanitize"                  % "1.3.11" exclude(
    "org.webjars", "angularjs"),
  "org.webjars"               %  "momentjs"                          % "2.10.3",
  // Testing
  "com.typesafe.akka"         %% "akka-testkit"                      % "2.3.11"             % "test",
  "org.scalatestplus"         %% "play"                              % "1.4.0-M3"           % "test",
  "org.pegdown"               % "pegdown"                            % "1.5.0"              % "test"
)

routesGenerator := InjectedRoutesGenerator

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

//net.virtualvoid.sbt.graph.Plugin.graphSettings

//MochaKeys.requires += "./setup.js"

// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

PB.protobufSettings

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray))

// setting for play-auto-refresh plugin so that it does not open a new browser window when
// the application is run
com.jamesward.play.BrowserNotifierKeys.shouldOpenBrowser := false

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*"
