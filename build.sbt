val akkaVer = "2.4.12"

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
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
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
  "dnvriend at bintray" at "http://dl.bintray.com/dnvriend/maven"
)

libraryDependencies ++= Seq(
  cache,
  filters,
  ( "com.typesafe.akka"         %% "akka-persistence"                    % akkaVer   % "compile"  ).excludeAll(ExclusionRule(organization="com.google.protobuf")),
  "com.typesafe.akka"           %% "akka-persistence-query-experimental" % akkaVer   % "compile",
  "com.typesafe.akka"           %% "akka-remote"                         % akkaVer   % "compile",
  ( "com.okumin"                %% "akka-persistence-sql-async"          % "0.4.0"   % "compile"  ).excludeAll(ExclusionRule(organization="com.typesafe.akka")),
  "org.scala-stm"               %% "scala-stm"                           % "0.8"     % "compile",
  "org.scalaz"                  %% "scalaz-core"                         % "7.2.7"   % "compile",
  "com.github.mauricio"         %% "mysql-async"                         % "0.2.20",
  "com.github.t3hnar"           %% "scala-bcrypt"                        % "3.0",
  "com.github.ancane"           %% "hashids-scala"                       % "1.2",
  "com.typesafe.play"           %% "play-mailer"                         % "5.0.0",
  "com.typesafe.scala-logging"  %% "scala-logging"                       % "3.5.0",
  "com.github.nscala-time"      %% "nscala-time"                         % "2.14.0",
  // WebJars infrastructure
  ( "org.webjars"               %% "webjars-play"                        % "2.5.0").exclude("org.webjars", "requirejs"),
  // WebJars dependencies
  "org.webjars"                 %  "requirejs"                           % "2.3.2",
  "org.webjars"                 %  "lodash"                              % "4.15.0",
  "org.webjars"                 %  "jquery"                              % "3.1.1",
  ( "org.webjars"               %  "bootstrap"                           % "3.3.7"  ).excludeAll(ExclusionRule(organization="org.webjars")),
  ( "org.webjars"               %  "angularjs"                           % "1.5.8"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"               %  "angular-ui-bootstrap"                % "2.2.0"  ).exclude("org.webjars", "angularjs"),
  ( "org.webjars"               %  "angular-ui-router"                   % "0.2.18" ).exclude("org.webjars", "angularjs"),
  ( "org.webjars"               %  "smart-table"                         % "2.1.3-1").exclude("org.webjars", "angularjs"),
  ( "org.webjars"               %  "toastr"                              % "2.1.2"  ).exclude("org.webjars", "jquery"),
  ( "org.webjars"               %  "angular-sanitize"                    % "1.3.11" ).exclude("org.webjars", "angularjs"),
  "org.webjars"                 %  "momentjs"                            % "2.16.0",
  "org.webjars"                 %  "sprintf.js"                          % "1.0.0",
  "org.webjars"                 %  "tv4"                                 % "1.0.17-1",
  "org.webjars.bower"           %  "angular-utils-ui-breadcrumbs"        % "0.2.2",
  "org.webjars.bower"           %  "bootstrap-ui-datetime-picker"        % "2.4.3",
  "org.webjars.bower"           %  "angular-gettext"                     % "2.2.1",
  // Testing
  ( "com.github.dnvriend"       %% "akka-persistence-inmemory"           % "1.3.16"  % "test" ).excludeAll(ExclusionRule(organization="com.typesafe.akka")),
  "com.typesafe.akka"           %% "akka-testkit"                        % akkaVer   % "test",
  "org.scalatestplus"           %% "play"                                % "1.4.0"   % "test",
  "org.pegdown"                 %  "pegdown"                             % "1.6.0"   % "test",
  "org.codehaus.janino"         %  "janino"                              % "3.0.6"   % "test"
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

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
  )

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
//wartremoverExcluded ++= ((crossTarget.value / "src_managed" / "main" / "compiled_protobuf" ) ** "*.scala").get

wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreDescriptionUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreDisabledEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEnabledEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreLocationUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "CentreNameUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "StudyAddedToCentreEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CentreEvents" / "StudyRemovedFromCentreEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventEvents" / "CollectionEventEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventEvents" / "CollectionEventEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventTypeEvents" / "CollectionEventTypeEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CollectionEventTypeEvents" / "CollectionEventTypeEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "Annotation.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "AnnotationType.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "AnnotationTypeRemoved.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "CommonEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "CommonEvents" / "Location.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ParticipantEvents" / "ParticipantEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ParticipantEvents" / "ParticipantEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ProcessingTypeEvents" / "ProcessingTypeEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ProcessingTypeEvents" / "ProcessingTypeEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentEvents" / "ShipmentEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentEvents" / "ShipmentEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentSpecimenEvents" / "ShipmentSpecimenEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "ShipmentSpecimenEvents" / "ShipmentSpecimenEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "SpecimenEvents" / "SpecimenEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "SpecimenEvents" / "SpecimenEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "CollectionEventTypeUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "ProcessingTypeUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenGroupUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkAnnotationTypeUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeAddedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeRemovedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "SpecimenLinkTypeUpdatedEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEventOld.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "StudyEvents" / "StudyEventsProto.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "UserEvents" / "UserEvent.scala"
wartremoverExcluded += crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "UserEvents" / "UserEventsProto.scala"
