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

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.11.8")

scalacOptions in Compile ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "deprecation",        // warning and location for usages of deprecated APIs
  "-feature",           // warning and location for usages of features that should be imported explicitly
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps",
  "-unchecked",          // additional warnings where generated code depends on assumptions
  "-Xlint:_",
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
)

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

//javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test ++=  Seq(
  "-Dconfig.file=conf/test.conf",
  "-Dlogger.resource=logback-test.xml"
)

javacOptions in ThisBuild  ++= Seq(
  "-source", "1.8",
  "-target", "1.8",
  "-Xlint"
)

javaOptions in run ++= Seq(
    "-Xms256M", "-Xmx2G", "-XX:+UseConcMarkSweepGC")

fork in run := true

testOptions in Test := Nil

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-h", "target/report")

(testOptions in Test) += Tests.Argument(TestFrameworks.ScalaTest, "-oDS")

resolvers ++= Seq(
  Classpaths.sbtPluginReleases,
  "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS"        at "https://oss.sonatype.org/content/repositories/releases",
  "Akka Snapshots"      at "http://repo.akka.io/snapshots/",
  "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"
)

libraryDependencies ++= Seq(
  cache,
  filters,
  ( "com.typesafe.akka"         %% "akka-persistence"                    % "2.4.7" % "compile" ).excludeAll(ExclusionRule(organization="com.google.protobuf")),
  "com.typesafe.akka"           %% "akka-persistence-query-experimental" % "2.4.7",
  "com.typesafe.akka"           %% "akka-remote"                         % "2.4.7"             % "compile",
  "com.typesafe.akka"           %% "akka-slf4j"                          % "2.4.7"             % "compile",
  "org.scala-stm"               %% "scala-stm"                           % "0.7"               % "compile",
  "org.scalaz"                  %% "scalaz-core"                         % "7.2.4"             % "compile",
  "org.iq80.leveldb"            % "leveldb"                              % "0.7",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"                       % "1.8",
  "com.github.t3hnar"           %% "scala-bcrypt"                        % "2.6",
  "com.github.ancane"           %% "hashids-scala"                       % "1.2",
  "com.typesafe.play"           %% "play-mailer"                         % "5.0.0-M1",
  "com.typesafe.scala-logging"  %% "scala-logging"                       % "3.4.0",
  "com.github.nscala-time"      %% "nscala-time"                         % "2.12.0",
  // WebJars infrastructure
  ( "org.webjars"               %% "webjars-play"                        % "2.5.0").exclude("org.webjars", "requirejs"),
  // WebJars dependencies
  "org.webjars"                 %  "requirejs"                           % "2.2.0",
  "org.webjars"                 %  "lodash"                              % "4.0.0",
  "org.webjars"                 %  "jquery"                              % "3.0.0",
  ( "org.webjars"               %  "bootstrap"                           % "3.3.6"  ).excludeAll(ExclusionRule(organization="org.webjars")),
  ( "org.webjars"               %  "angularjs"                           % "1.5.6"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"               %  "angular-ui-bootstrap"                % "1.3.3" ).exclude("org.webjars", "angularjs"),
  ( "org.webjars"               %  "angular-ui-router"                   % "0.2.18" ).exclude("org.webjars", "angularjs"),
  "org.webjars"                 %  "smart-table"                         % "2.1.3-1",
  ( "org.webjars"               %  "toastr"                              % "2.1.2"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"               %  "angular-sanitize"                    % "1.3.11" ).exclude("org.webjars", "angularjs"),
  "org.webjars"                 %  "momentjs"                            % "2.13.0",
  "org.webjars"                 %  "sprintf.js"                          % "1.0.0",
  "org.webjars"                 %  "tv4"                                 % "1.0.17-1",
  "org.webjars.bower"           %  "angular-utils-ui-breadcrumbs"        % "0.2.2",
  "org.webjars.bower"           %  "bootstrap-ui-datetime-picker"        % "2.4.0",
  // Testing
  "com.github.dnvriend"         %% "akka-persistence-inmemory"           % "1.3.0"              % "test",
  "com.typesafe.akka"           %% "akka-testkit"                        % "2.4.7"              % "test",
  "org.scalatestplus"           %% "play"                                % "1.4.0"              % "test",
  "org.pegdown"                 %  "pegdown"                             % "1.6.0"              % "test",
  "org.codehaus.janino"         %  "janino"                              % "2.7.8"              % "test"
  )

incOptions := incOptions.value.withNameHashing(true)

routesGenerator := InjectedRoutesGenerator

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

//EclipseKeys.withSource := true

//net.virtualvoid.sbt.graph.Plugin.graphSettings

//MochaKeys.requires += "./setup.js"

// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

PB.scalapbVersion := "0.5.31"
PB.protobufSettings

// Protocol buffers compiler - used by ScalaPB
PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray))

// setting for play-auto-refresh plugin so that it does not open a new browser window when
// the application is run
com.jamesward.play.BrowserNotifierKeys.shouldOpenBrowser := false

coverageExcludedPackages := "<empty>;Reverse.*"

wartremoverErrors in (Compile, compile) ++= Warts.allBut(Wart.NoNeedForMonad, Wart.Equals, Wart.ToString)

wartremoverExcluded += crossTarget.value / "routes" / "main" / "router" / "Routes.scala"
wartremoverExcluded += crossTarget.value / "routes" / "main" / "router" / "RoutesPrefix.scala"
wartremoverExcluded += crossTarget.value / "routes" / "main" / "controllers" / "ReverseRoutes.scala"
wartremoverExcluded += crossTarget.value / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala"

// see following for explanation: https://github.com/puffnfresh/wartremover/issues/219
wartremoverExcluded ++= ((sourceManaged.value / "main" / "compiled_protobuf" / "org" / "biobank" / "infrastructure") ** "*.scala").get
