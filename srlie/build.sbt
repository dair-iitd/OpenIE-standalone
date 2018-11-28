import AssemblyKeys._

assemblySettings

ReleaseSettings.defaults

name := "srlie"

organization := "edu.washington.cs.knowitall.srlie"

crossScalaVersions := Seq("2.10.3")

scalaVersion <<= crossScalaVersions { (vs: Seq[String]) => vs.head }

resolvers += "Sonatype SNAPSHOTS" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Release" at "https://oss.sonatype.org/service/local/repositories/releases/content/"


libraryDependencies ++= Seq(
"edu.washington.cs.knowitall.nlptools" %% "nlptools-srl-clear" % "2.4.5" ,
  "edu.washington.cs.knowitall.nlptools" %% "nlptools-conf-breeze" % "2.4.5",
  "com.github.scopt" %% "scopt" % "2.1.0" cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2"
      case "2.10.2" => "2.10"
      case x => x
    },
  "org.slf4j" % "slf4j-api" % "1.7.3",
  "org.scalaz" %% "scalaz-core" % "7.0.3" cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2"
      case "2.10.2" => "2.10"
      case x => x
    },
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "test",
  "ch.qos.logback" % "logback-core" % "1.0.13" % "test",
  "junit" % "junit" % "4.11" % "test",
    "org.specs2" % "specs2" % "1.12.3" % "test" cross CrossVersion.binaryMapped {
      case "2.9.3" => "2.9.2"
      case "2.10.2" => "2.10"
      case x => x
    })

mainClass in assembly := Some("edu.knowitall.srlie.SrlExtractor")

scalacOptions ++= Seq("-unchecked", "-deprecation")

licenses := Seq("Ollie Software License Agreement" -> url("https://raw.github.com/knowitall/ollie/master/LICENSE"))

homepage := Some(url("https://github.com/knowitall/openie-srl"))

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
    <url>https://github.com/knowitall/openie-srl</url>
    <connection>scm:git://github.com/knowitall/openie-srl.git</connection>
    <developerConnection>scm:git:git@github.com:knowitall/openie-srl.git</developerConnection>
    <tag>HEAD</tag>
  </scm>
  <developers>
   <developer>
      <name>Michael Schmitz</name>
    </developer>
  </developers>)
