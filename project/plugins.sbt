// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.2")

// addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.0")

// dependency graph generator
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
