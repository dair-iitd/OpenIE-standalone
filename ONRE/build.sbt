releaseSettings

ReleaseSettings.defaults

name := "onre"

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}

organization := "edu.iitd.cse.open_nre.onre" //TODO

crossScalaVersions := Seq("2.10.2")

scalaVersion <<= crossScalaVersions { (vs: Seq[String]) => vs.head }

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "CogCompSoftware" at "https://cogcomp.seas.upenn.edu/m2repo/"

libraryDependencies ++= Seq(
  //"org.scala-lang" %% "scala-library" % "2.11.8",
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-parse-clear" % "2.4.5",
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-core" % "2.4.5",
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-stem-morpha" % "2.4.5",
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
  "edu.illinois.cs.cogcomp" % "illinois-quantifier" % "2.0.1",
  "edu.illinois.cs.cogcomp" % "LBJava" % "1.0.3",
  "edu.illinois.cs.cogcomp" % "illinois-pos" % "2.0.0",
  "edu.illinois.cs.cogcomp" % "edison" % "0.7.1",
  "edu.mit" % "jwi" % "2.2.3",
  "com.google.code.gson" % "gson" % "2.6.2",
  "edu.illinois.cs.cogcomp" % "illinois-core-utilities" % "1.2.9",
  "net.sf.trove4j" % "trove4j" % "2.1.0"



  //"org.scalatest" % "scalatest_2.10" % "2.0.RC1" % "test")
)

mainClass in assembly := Some("edu.iitd.cse.open_nre.onre.TestMain") //TODO

scalacOptions ++= Seq("-unchecked", "-deprecation")

// custom options for high memory usage

javaOptions += "-Xmx4G"

javaOptions += "-XX:+UseConcMarkSweepGC"

fork in run := true

fork in Test := true

connectInput in run := true // forward stdin/out to fork

licenses := Seq("Open IE Software License Agreement" -> url("https://raw.github.com/Open-NRE/ONRE/master/LICENSE"))  //TODO

homepage := Some(url("https://github.com/Open-NRE/ONRE")) //TODO

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra := (
  <scm>
    <url>https://github.com/knowitall/openie</url>
    <connection>scm:git://github.com/Open-NRE/ONRE.git</connection> //TODO
    <developerConnection>scm:git:git@github.com:Open-NRE/ONRE.git</developerConnection> //TODO
    <tag>HEAD</tag>
  </scm>
  <developers>
   <developer>
      <name>Harinder Pal</name>
    </developer>
  </developers>)

packagerSettings

packageArchetype.java_application

mappings in Universal ++= Seq(
  file("README.md") -> "README.md",
  file("LICENSE") -> "LICENSE"
)
