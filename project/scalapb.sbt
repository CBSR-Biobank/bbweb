addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.1")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.47",
  "com.github.os72" % "protoc-jar" % "3.1.0"
)
