libraryDependencies ++= Seq(
    "com.trueaccord.scalapb" %% "compilerplugin-shaded" % "0.6.2",
    "com.github.os72" % "protoc-jar" % "3.1.0"
)

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.11")
