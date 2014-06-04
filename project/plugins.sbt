// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

// addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.0")

// dependency graph generator
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

// plugin for Play Framework apps which works with a Chrome Extension to auto-refresh your browser
// when changes are made to the web app
addSbtPlugin("com.jamesward" %% "play-auto-refresh" % "0.0.7")

