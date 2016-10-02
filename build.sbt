name := "elastic-scala"
 
version := "1.0-SNAPSHOT"
 
scalaVersion := "2.11.8"

organization := "ml.generall"


libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies += "com.sksamuel.elastic4s" %% "elastic4s-core" % "2.3.0"

libraryDependencies += "ml.generall" %% "scala-common" % "1.0-SNAPSHOT"


resolvers += Resolver.mavenLocal
