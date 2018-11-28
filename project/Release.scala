import sbt._
import Keys._

import sbtrelease._
import ReleaseStateTransformations._
import Utilities._
import sbtrelease.ReleasePlugin.autoImport._
import com.typesafe.sbt.pgp.PgpKeys._

object ReleaseSettings {
    val defaults = Seq(
        releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts.copy(action = publishSignedAction),
        setNextVersion,
        commitNextVersion
    ))

  lazy val publishSignedAction = { st: State =>
    val extracted = st.extract
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, st)
  }
}
