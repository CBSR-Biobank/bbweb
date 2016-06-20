addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.31")

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.31",
  "com.github.os72" % "protoc-jar" % "3.0.0-b3"
)
