import NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager.Universal

assemblyMergeStrategy in assembly := {
    case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
    case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
    case x => 
       val oldStrategy = (assemblyMergeStrategy in assembly).value
       oldStrategy(x)
}

ReleaseSettings.defaults

name := "openie"

organization := "edu.washington.cs.knowitall.openie"

crossScalaVersions := Seq("2.10.2")
//scalaVersion := crossScalaVersions { (vs: Seq[String]) => vs.head.value }
scalaVersion := "2.10.2"

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += Resolver.bintrayIvyRepo("com.eed3si9n", "sbt-plugins")

libraryDependencies ++= Seq(
  // extractor components
  "edu.washington.cs.knowitall.srlie" %% "srlie" % "1.0.4-SNAPSHOT",
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

//dependencyOverrides ++= Set( // Seq for SBT 1.0.x
//  "com.google.guava" %% "guava" % "20.0",
//  "org.eclipse.sisu" %% "org.eclipse.sisu.plexus" % "0.3.2",
//  "org.codehaus.plexus" %% "plexus-utils" % "3.0.22"
//)

mainClass in assembly := Some("edu.knowitall.openie.OpenIECli")

scalacOptions ++= Seq("-unchecked", "-deprecation")

// custom options for high memory usage

javaOptions += "-Xmx10G"

javaOptions += "-XX:+UseConcMarkSweepGC"

fork in run := true

fork in Test := true

outputStrategy := Some(StdoutOutput)

connectInput in run := true // forward stdin/out to fork

licenses := Seq("Open IE Software License Agreement" -> url("https://raw.github.com/knowitall/openie/master/LICENSE"))

homepage := Some(url("https://github.com/knowitall/openie"))

publishMavenStyle := true

//publishTo <<= version { (v: String) =>
//  val nexus = "https://oss.sonatype.org/"
//  if (v.trim.endsWith("SNAPSHOT"))
//    Some("snapshots" at nexus + "content/repositories/snapshots")
//  else
//    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>https://github.com/knowitall/openie</url>
    <connection>scm:git://github.com/knowitall/openie.git</connection>
    <developerConnection>scm:git:git@github.com:knowitall/openie.git</developerConnection>
    <tag>HEAD</tag>
  </scm>
  <developers>
   <developer>
      <name>Michael Schmitz</name>
    </developer>
    <developer>
      <name>Bhadra Mani</name>
    </developer>
  </developers>)

//packagerSettings

//packageArchetype.java_application
enablePlugins(JavaAppPackaging)
mappings in Universal ++= Seq(
  file("README.md") -> "README.md",
  file("LICENSE") -> "LICENSE"
)

