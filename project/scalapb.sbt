addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.43")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.43",
  "com.github.os72" % "protoc-jar" % "3.1.0"
)
