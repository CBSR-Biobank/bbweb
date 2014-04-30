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
  "com.typesafe.akka"   %% "akka-persistence-experimental"  % "2.3.1"              % "compile",
  "com.github.ddevore"  %% "akka-persistence-mongo-casbah"  % "0.6-SNAPSHOT"       % "compile",
  "com.typesafe"        %% "jse"                            % "1.0.0-M2"           % "compile",
  "com.typesafe"        %% "npm"                            % "1.0.0-M2"           % "compile",
  "com.typesafe.akka"   %% "akka-slf4j"                     % "2.3.1"              % "compile",
  "com.typesafe.akka"   %% "akka-testkit"                   % "2.3.1"              % "test",
  "org.scala-stm"       %% "scala-stm"                      % "0.7"                % "compile",
  "org.scalatest"       %% "scalatest"                      % "2.1.2"              % "test",
  "org.scalaz"          %% "scalaz-core"                    % "7.0.6"              % "compile",
  "org.webjars"          % "angularjs"                      % "1.2.16"             % "compile",
  "org.webjars"          % "bootstrap"                      % "3.1.1"              % "compile",
  "org.webjars"          % "requirejs"                      % "2.1.11"             % "compile",
  "org.webjars"          % "webjars-play_2.10"              % "2.3-M1"             % "compile",
  "ws.securesocial"     %% "securesocial"                   % "play-2.3-SNAPSHOT"  % "compile"
  //"com.typesafe" %% "webdriver" % "1.0.0-M2",
  //"com.typesafe.akka" %% "akka-testkit" % "2.3.2" % "test",
  //"com.typesafe.play" %% "play-slick" % "0.6.0.1",
)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

lazy val root = (project in file(".")).addPlugins(PlayScala).addPlugins(SbtWeb)

net.virtualvoid.sbt.graph.Plugin.graphSettings

