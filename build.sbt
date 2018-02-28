import AssemblyKeys._
import NativePackagerKeys._

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
    case x => old(x)
  } 
}

ReleaseSettings.defaults

name := "openie"

organization := "edu.washington.cs.knowitall.openie"

crossScalaVersions := Seq("2.10.2")

scalaVersion <<= crossScalaVersions { (vs: Seq[String]) => vs.head }

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  // extractor components
  "edu.washington.cs.knowitall.srlie" %% "srlie" % "1.0.3",
  "edu.washington.cs.knowitall.chunkedextractor" %% "chunkedextractor" % "2.2.1",
  // for splitting sentences
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-sentence-opennlp" % "2.4.5",
  // for remote components
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  // resource management
  "com.jsuereth" %% "scala-arm" % "1.3",
  // logging
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  "org.scalatest" % "scalatest_2.10" % "2.0.RC1" % "test")

mainClass in assembly := Some("edu.knowitall.openie.OpenIECli")

scalacOptions ++= Seq("-unchecked", "-deprecation")

// custom options for high memory usage

javaOptions += "-Xmx10G"

javaOptions += "-XX:+UseConcMarkSweepGC"

fork in run := true

fork in Test := true

connectInput in run := true // forward stdin/out to fork

licenses := Seq("Open IE 4 Software License Agreement" -> url("https://raw.githubusercontent.com/knowitall/openie/master/LICENSE"))

homepage := Some(url("https://github.com/dair-iitd/OpenIE-standalone"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/dair-iitd/OpenIE-standalone"),
    "scm:git@github.com:dair-iitd/OpenIE-standalone.git"
  )
)

developers := List(
  Developer(
    id    = "Your identifier",
    name  = "Michael Schmitz",
    email = "your@email",
    url   = url("http://your.url")
  ),
  Developer(
    id    = "Your identifier",
    name  = "Bhadra Mani",
    email = "your@email",
    url   = url("http://your.url")
  ),
  Developer(
    id    = "swarnaHub",
    name  = "Swarnadeep Saha",
    email = "your@email",
    url   = url("https://www.linkedin.com/in/swarnadeep-saha-b43a617b?trk=hp-identity-name")
  ),
  Developer(
    id    = "harrysethi",
    name  = "Harinder Sethi",
    email = "sethi.harinder@gmail.com",
    url   = url("https://about.me/harinder.pal")
  ),
)

publishMavenStyle := true

useGpg := true

pomIncludeRepository := { _ => false }

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

packagerSettings

packageArchetype.java_application

mappings in Universal ++= Seq(
  file("README.md") -> "README.md",
  file("LICENSE") -> "LICENSE"
)

