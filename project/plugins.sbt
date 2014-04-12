// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3-M1")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0-M2a")

// addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.0")

// dependency graph generator
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
