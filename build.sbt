import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

val akkaVer = "2.5.3"
val angularVer = "1.6.5"

name := "bbweb"

organization in ThisBuild := "org.biobank"

def excludeSpecs2(module: ModuleID): ModuleID =
  module.excludeAll(ExclusionRule(organization = "org.specs2", name = "specs2"))
    .exclude("com.novocode", "junit-interface")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, DebianPlugin)
  .settings(libraryDependencies ~= (_.map(excludeSpecs2)))

maintainer in Linux := "Canadian BioSample Repository <tech@biosample.ca>"

packageSummary in Linux := "Biorepository application for tracking biospecimens."

packageDescription := "Biorepository application for tracking biospecimens."

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.12.2")

scalacOptions in Compile ++= Seq(
    "-target:jvm-1.8",
    "-encoding", "UTF-8",
    "-deprecation",       // warning and location for usages of deprecated APIs
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
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:-params",
    "-Ywarn-unused-import",
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused
  )

scalacOptions in (Compile,doc) ++= Seq("-groups", "-implicits")

fork in Test := true

//javaOptions ++= Seq("-Xmx1024M", "-XX:MaxPermSize=512m")

javaOptions in Test ++=  Seq(
    "-Xms512M",
    "-Xmx2048M",
    "-XX:+CMSClassUnloadingEnabled",
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
    Resolver.jcenterRepo
  )

libraryDependencies ++= Seq(
    guice,
    ehcache,
    filters,
    "org.scala-stm"               %% "scala-stm"                           % "0.8",
    "com.typesafe.play"           %% "play-json"                           % "2.6.2",
    ( "com.typesafe.akka"         %% "akka-persistence"                    % "2.5.3"   % "compile"  ).excludeAll(ExclusionRule(organization="com.google.protobuf")),
    "com.typesafe.akka"           %% "akka-persistence-query"              % "2.5.3"   % "compile",
    "com.typesafe.akka"           %% "akka-remote"                         % akkaVer   % "compile",
    ( "com.okumin"                %% "akka-persistence-sql-async"          % "0.4.0"   % "compile"  ).excludeAll(ExclusionRule(organization="com.typesafe.akka")),
    "org.scalaz"                  %% "scalaz-core"                         % "7.2.14"  % "compile",
    "com.github.mauricio"         %% "mysql-async"                         % "0.2.21",
    "com.github.t3hnar"           %% "scala-bcrypt"                        % "3.1",
    "com.github.ancane"           %% "hashids-scala"                       % "1.3",
    "com.typesafe.play"           %% "play-mailer"                         % "6.0.0",
    "com.typesafe.play"           %% "play-mailer-guice"                   % "6.0.0",
    "com.typesafe.scala-logging"  %% "scala-logging"                       % "3.7.1",
    "com.github.nscala-time"      %% "nscala-time"                         % "2.16.0",
    // WebJars infrastructure
    ( "org.webjars"               %% "webjars-play"                        % "2.6.1").exclude("org.webjars", "requirejs"),
    // WebJars dependencies
    "org.webjars"                 %  "requirejs"                           % "2.3.3",
    "org.webjars.npm"             %  "angular"                             % angularVer,
    "org.webjars.npm"             %  "angular-animate"                     % "1.6.4",
    "org.webjars.npm"             %  "angular-cookies"                     % "1.6.4",
    ( "org.webjars.bower"         %  "angular-gettext"                     % "2.2.1" ).exclude("org.webjars.bower", "angular"),
    "org.webjars.npm"             %  "angular-messages"                    % angularVer,
    "org.webjars.npm"             %  "angular-sanitize"                    % angularVer,
    "org.webjars.npm"             %  "angular-smart-table"                 % "2.1.6",
    "org.webjars.npm"             %  "angular-toastr"                      % "1.7.0",
    "org.webjars.npm"             %  "angular-ui-bootstrap"                % "2.5.0",
    ( "org.webjars.npm"           % "angular-ui-router"                    % "1.0.3" ).exclude("org.webjars.npm", "angular"),
    "org.webjars.npm"             %  "bootstrap"                           % "3.3.7",
    ( "org.webjars.bower"         %  "bootstrap-ui-datetime-picker"        % "2.6.0" ).exclude("org.webjars.bower", "angular"),
    "org.webjars.npm"             %  "jquery"                              % "3.2.1",
    "org.webjars.npm"             %  "lodash"                              % "4.17.4",
    "org.webjars.npm"             %  "moment"                              % "2.18.1",
    "org.webjars.npm"             %  "sprintf-js"                          % "1.0.3",
    "org.webjars.npm"             %  "tv4"                                 % "1.3.0",
    // Testing
    ( "com.github.dnvriend"       %% "akka-persistence-inmemory"           % "2.5.1.1"  % "test" ).excludeAll(ExclusionRule(organization="com.typesafe.akka")),
    "com.typesafe.akka"           %% "akka-testkit"                        % akkaVer   % "test",
    "org.scalatestplus.play"      %% "scalatestplus-play"                  % "3.1.0"   % "test",
    "org.pegdown"                 %  "pegdown"                             % "1.6.0"   % "test",
    "org.codehaus.janino"         %  "janino"                              % "3.0.7"   % "test",
    "org.mockito"                 %  "mockito-core"                        % "2.8.47"  % "test"
  )

incOptions := incOptions.value.withNameHashing(true)

routesGenerator := InjectedRoutesGenerator

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node


// Configure the steps of the asset pipeline (used in stage and dist tasks)
// rjs = RequireJS, uglifies, shrinks to one file, replaces WebJars with CDN
// digest = Adds hash to filename
// gzip = Zips all assets, Asset controller serves them automatically when client accepts them
pipelineStages := Seq(rjs, digest, gzip)

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value
  )

// setting for play-auto-refresh plugin so that it does not open a new browser window when
// the application is run
com.jamesward.play.BrowserNotifierKeys.shouldOpenBrowser := false

coverageExcludedPackages := "<empty>;router.*;views.html.*;Reverse.*;org.biobank.infrastructure.event.*;org.biobank.TestData"

wartremoverErrors in (Compile, compile) ++= Warts.allBut(Wart.ArrayEquals, Wart.Nothing, Wart.Equals, Wart.ToString)

// see following for explanation: https://github.com/puffnfresh/wartremover/issues/219
wartremoverExcluded ++= ((crossTarget.value / "src_managed" / "main" / "compiled_protobuf" ) ** "*.scala").get

wartremoverExcluded ++= Seq(
    crossTarget.value / "routes" / "main" / "router" / "Routes.scala",
    crossTarget.value / "routes" / "main" / "router" / "RoutesPrefix.scala",
    crossTarget.value / "routes" / "main" / "controllers" / "ReverseRoutes.scala",
    crossTarget.value / "routes" / "main" / "controllers" / "javascript" / "JavaScriptReverseRoutes.scala",

    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "AccessEvents" / "AccessEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "AccessEvents" / "AccessEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreDescriptionUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreDisabledEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEnabledEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreNameUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "StudyAddedToCentreEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "StudyRemovedFromCentreEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventEvents" / "CollectionEventEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventEvents" / "CollectionEventEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventTypeEvents" / "CollectionEventTypeEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventTypeEvents" / "CollectionEventTypeEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "Annotation.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "AnnotationType.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "AnnotationTypeRemoved.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "CommonEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "Location.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ParticipantEvents" / "ParticipantEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ParticipantEvents" / "ParticipantEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ProcessingTypeEvents" / "ProcessingTypeEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ProcessingTypeEvents" / "ProcessingTypeEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentEvents" / "ShipmentEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentEvents" / "ShipmentEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentSpecimenEvents" / "ShipmentSpecimenEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentSpecimenEvents" / "ShipmentSpecimenEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "SpecimenEvents" / "SpecimenEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "SpecimenEvents" / "SpecimenEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeAddedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeRemovedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeUpdatedEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEventOld.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEventsProto.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "UserEvents" / "UserEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "UserEvents" / "UserEventsProto.scala"
  )
