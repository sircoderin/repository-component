name := "repository-component"
organization := "dot.cpp"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  "org.jetbrains" % "annotations" % "24.0.1",
  "dev.morphia.morphia" % "morphia-core" % "2.3.5",
  "com.github.victools" % "jsonschema-generator" % "4.38.0",
  "com.github.victools" % "jsonschema-module-jackson" % "4.38.0",
  "com.github.victools" % "jsonschema-module-javax-validation" % "4.38.0"
)


Global / onChangedBuildSource := ReloadOnSourceChanges
jcheckStyleConfig := "google-checks.xml"

// Skip Javadoc generation during publishLocal.
// Works around an sbt DiagnosticsReporter crash (StringIndexOutOfBoundsException)
// triggered by CRLF line endings when javac emits doclint warnings.
Compile / doc / sources := Seq.empty
Compile / packageDoc / publishArtifact := false

// compile will run formatter and checkstyle on app files and test files
(Compile / compile) := ((Compile / compile) dependsOn (Compile / javafmt)).value
(Compile / compile) := ((Compile / compile) dependsOn (Compile / jcheckStyle)).value
(Compile / compile) := ((Compile / compile) dependsOn (Test / jcheckStyle)).value
