addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.11")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.0",
  "com.github.os72" % "protoc-jar" % "3.1.0"
)
