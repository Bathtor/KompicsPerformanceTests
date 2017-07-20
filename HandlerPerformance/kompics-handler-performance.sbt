name := "Kompics-Handler-Performance"

organization := "se.sics.kompics"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.12.2"

scalacOptions ++= Seq("-deprecation","-feature")


resolvers += Resolver.mavenLocal
resolvers += "Kompics Releases" at "http://kompics.sics.se/maven/repository/"
resolvers += "Kompics Snapshots" at "http://kompics.sics.se/maven/snapshotrepository/"

libraryDependencies += "se.sics.kompics" %% "kompics-scala" % version.value
libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.3"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.3"
libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.3.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" 
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" 

parallelExecution in Test := false

EclipseKeys.withSource := false

mainClass in assembly := Some("se.sics.kompics.performancetest.Main")