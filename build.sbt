organization := "org.etypedactors"

name := "etypedactors"

version := "0.2-SNAPSHOT"

resolvers += "Akka" at "http://akka.io/repository"

resolvers += "Guiceyfruit" at "http://guiceyfruit.googlecode.com/svn/repo/releases/"

libraryDependencies ++= Seq(
  "se.scalablesolutions.akka" % "akka-actor" % "1.2"
)

ivyConfigurations += Default.extend (Compile, Sources, Docs)

artifact in (Compile, packageDoc) ~= { _.copy(`type` = "javadoc") }

artifact in (Compile, packageSrc) ~= { _.copy(`type` = "source") }

