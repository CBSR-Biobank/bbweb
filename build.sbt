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

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.5")

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
  //"Akka Snapshots"               at "https://repo.akka.io/snapshots/",
  "Sonatype OSS Snapshots"       at "https://oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= Seq(
  cache,
  filters,
  "com.typesafe.akka"         %% "akka-persistence-experimental"  % "2.3.4"              % "compile",
  "com.typesafe.akka"         %% "akka-remote"                    % "2.3.4"              % "compile",
  "com.typesafe.akka"         %% "akka-slf4j"                     % "2.3.2"              % "compile",
  "org.scala-stm"             %% "scala-stm"                      % "0.7"                % "compile",
  "org.scalaz"                %% "scalaz-core"                    % "7.1.0"              % "compile",
  //"org.scalaz"                %% "scalaz-typelevel"               % "7.1.0"              % "compile",
  "com.github.ddevore"        %% "akka-persistence-mongo-casbah"  % "0.7.3-SNAPSHOT"     % "compile",
  "com.github.t3hnar"         %% "scala-bcrypt"                   % "2.4",
  "com.typesafe.play.plugins" %% "play-plugins-mailer"            % "2.3.0",
  "org.scaldi"                %% "scaldi-play"                    % "0.4.1",
  "org.scaldi"                %% "scaldi-akka"                    % "0.4",
  // WebJars infrastructure
  "org.webjars"               %  "webjars-locator"                % "0.19" ,
  "org.webjars"               %% "webjars-play"                   % "2.3.0-2",
  // WebJars dependencies
  "org.webjars"               %  "requirejs"                      % "2.1.15",
  "org.webjars"               %  "underscorejs"                   % "1.7.0",
  "org.webjars"               %  "jquery"                         % "2.1.1",
  "org.webjars"               %  "bootstrap"                      % "3.3.1" exclude(
    "org.webjars", "jquery"),
  "org.webjars"               %  "angularjs"                      % "1.3.6" exclude(
    "org.webjars", "jquery"),
  "org.webjars"               %  "angular-ui-bootstrap"           % "0.12.0",
  "org.webjars"               %  "angular-strap"                  % "2.1.4",
  "org.webjars"               %  "angular-ui-router"              % "0.2.13" exclude(
    "org.webjars", "angularjs"),
  "org.webjars"               %  "ng-table"                       % "0.3.3",
  "org.webjars"               % "toastr"                          % "2.1.0"exclude(
    "org.webjars", "jquery"),
  "org.webjars"               % "angular-sanitize"                % "1.3.0-beta.18" exclude(
    "org.webjars", "angularjs"),
  // Testing
  "com.typesafe.akka"         %% "akka-testkit"                   % "2.3.2"              % "test",
  "org.scalatestplus"         %% "play"                           % "1.2.0"              % "test",
  "org.pegdown"               % "pegdown"                         % "1.0.2"              % "test"
)

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

// not sure if emojiLogs actually works with play
emojiLogs

PB.protobufSettings
