// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.15")

// Defines scaffolding (found under .g8 folder)
// http://www.foundweekends.org/giter8/scaffolding.html
// sbt "g8Scaffold form"
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.13.1")

// formats code
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.6.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.15.0")
