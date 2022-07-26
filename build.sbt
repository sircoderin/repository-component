name := "repository-component"
organization := "dot.cpp"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  "dev.morphia.morphia" % "morphia-core" % "2.2.7",
  "com.github.victools" % "jsonschema-generator" % "4.16.0",
  "com.github.victools" % "jsonschema-module-jackson" % "4.16.0",
  "com.github.victools" % "jsonschema-module-javax-validation" % "4.16.0"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

Global / onChangedBuildSource := ReloadOnSourceChanges
jcheckStyleConfig := "checkstyle-config.xml"

// compile will run checkstyle on app files and test files
(Compile / compile) := ((Compile / compile) dependsOn (Compile / jcheckStyle)).value
(Compile / compile) := ((Compile / compile) dependsOn (Test / jcheckStyle)).value
