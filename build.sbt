import com.typesafe.config._

val conf = ConfigFactory.parseFile(new File("conf/application.conf")).resolve()

version := conf.getString("app.version")

val akkaVer = "2.5.6"
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

scalaVersion := Option(System.getProperty("scala.version")).getOrElse("2.12.4")

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
    "-Ywarn-unused:params",
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

addCompilerPlugin("com.github.ghik" %% "silencer-plugin" % "0.5")

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
    "com.typesafe.play"           %% "play-json"                           % "2.6.7",
    ( "com.typesafe.akka"         %% "akka-persistence"                    % akkaVer   % "compile"  )
      .excludeAll(ExclusionRule(organization="com.google.protobuf")),
    "com.typesafe.akka"           %% "akka-persistence-query"              % akkaVer   % "compile",
    "com.typesafe.akka"           %% "akka-remote"                         % akkaVer   % "compile",
    ( "com.github.dnvriend"       %% "akka-persistence-jdbc"               % "3.0.1" % "compile"  )
      .excludeAll(ExclusionRule(organization="com.typesafe.akka")),
    "mysql"                       % "mysql-connector-java"                 % "8.0.8-dmr",
    "org.scalaz"                  %% "scalaz-core"                         % "7.2.16"  % "compile",
    "com.github.mauricio"         %% "mysql-async"                         % "0.2.21",
    "com.github.t3hnar"           %% "scala-bcrypt"                        % "3.1",
    "com.github.ancane"           %% "hashids-scala"                       % "1.3",
    "com.typesafe.play"           %% "play-mailer"                         % "6.0.1",
    "com.typesafe.play"           %% "play-mailer-guice"                   % "6.0.1",
    "com.typesafe.scala-logging"  %% "scala-logging"                       % "3.7.2",
    "com.github.ghik"             %% "silencer-lib"                        % "0.5"  % "compile",
    // Testing
    ( "com.github.dnvriend"       %% "akka-persistence-inmemory"           % "2.5.1.1"  % "test" )
      .excludeAll(ExclusionRule(organization="com.typesafe.akka")),
    "com.typesafe.akka"           %% "akka-testkit"                        % akkaVer   % "test",
    "org.scalatestplus.play"      %% "scalatestplus-play"                  % "3.1.2"   % "test",
    "org.pegdown"                 %  "pegdown"                             % "1.6.0"   % "test",
    "org.codehaus.janino"         %  "janino"                              % "3.0.7"   % "test",
    "org.mockito"                 %  "mockito-core"                        % "2.11.0"  % "test"
  )

routesGenerator := InjectedRoutesGenerator

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")

PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value
  )

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
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "MembershipEvents" / "MembershipEvent.scala",
    crossTarget.value / "src_managed" / "main" / "org" / "biobank" / "infrastructure" / "event" / "MembershipEvents" / "MembershipEventsProto.scala",
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
