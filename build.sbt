name := """repository-component"""
organization := "dot.cpp"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  "org.mongodb" % "mongo-java-driver" % "3.12.0",
  "dev.morphia.morphia" % "core" % "1.5.8",
  "com.github.victools" % "jsonschema-generator" % "4.16.0",
  "com.github.victools" % "jsonschema-module-jackson" % "4.16.0",
  "com.github.victools" % "jsonschema-module-javax-validation" % "4.16.0",
  "com.google.code.gson" % "gson" % "2.8.2"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

jcheckStyleConfig := "checkstyle-config.xml"

// compile will run checkstyle on app files and test files
(Compile / compile) := ((Compile / compile) dependsOn (Compile / jcheckStyle)).value
(Compile / compile) := ((Compile / compile) dependsOn (Test / jcheckStyle)).value
