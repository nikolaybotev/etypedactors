organization := "org.etypedactors"

name := "etypedactors"

version := "0.3-SNAPSHOT"

resolvers += "Akka" at "http://repo.akka.io/releases"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor" % "2.0.1"
)

//ivyConfigurations += Default.extend (Compile, Sources, Docs)

//artifact in (Compile, packageDoc) ~= { _.copy(`type` = "javadoc") }

//artifact in (Compile, packageSrc) ~= { _.copy(`type` = "source") }

EclipseKeys.withSource := true

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE17)
