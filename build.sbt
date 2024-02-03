name := "repository-component"
organization := "dot.cpp"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.10"

libraryDependencies ++= Seq(
  guice,
  "org.jetbrains" % "annotations" % "24.0.1",
  "org.slf4j" % "slf4j-api" % "2.0.6",
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "dev.morphia.morphia" % "morphia-core" % "2.3.5",
  "com.github.victools" % "jsonschema-generator" % "4.28.0",
  "com.github.victools" % "jsonschema-module-jackson" % "4.28.0",
  "com.github.victools" % "jsonschema-module-javax-validation" % "4.28.0"
)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

Global / onChangedBuildSource := ReloadOnSourceChanges
jcheckStyleConfig := "google-checks.xml"

// compile will run checkstyle on app files and test files
(Compile / compile) := ((Compile / compile) dependsOn (Compile / jcheckStyle)).value
(Compile / compile) := ((Compile / compile) dependsOn (Test / jcheckStyle)).value
