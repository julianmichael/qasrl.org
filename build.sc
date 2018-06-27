import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import coursier.maven.MavenRepository
import ammonite.ops._

val thisScalaVersion = "2.12.6"
val thisScalaJSVersion = "0.6.23"
val scalatagsVersion = "0.6.7"
val catsVersion = "1.1.0"
val scalacssVersion = "0.5.3"
val monocleVersion = "1.4.0"

val scalajsDomVersion = "0.9.0"
val scalajsJqueryVersion = "0.9.0"
val scalajsReactVersion = "1.1.0"
val scalajsScalaCSSVersion = "0.5.3"

// workaround for source project dependency
import $file.lib.sitegen.{build => SitegenBuild}
object sitegen extends SitegenBuild.SitegenTrait {
  def sources = T.sources {
    build.millSourcePath / 'lib / 'sitegen / 'sitegen / 'src
  }
}

trait CommonModule extends ScalaModule {

  def scalaVersion = thisScalaVersion

  def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Ypartial-unification"
  )

  // resolvers sonatype snapshots
  // def repositories = super.repositories ++ Seq(
  //   MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  // )

  def ivyDeps = super.ivyDeps() ++ Agg(
    // ivy"com.lihaoyi::scalatags::$scalatagsVersion",
    // ivy"org.typelevel::cats-core::$catsVersion",
    // ivy"com.github.japgolly.scalacss::core:$scalacssVersion",
    // ivy"com.github.japgolly.scalacss::ext-scalatags:$scalacssVersion"
  )
}

import $file.lib.sitegen.scripts.ScalatexBuild
object qasrlsite extends CommonModule with ScalatexBuild.ScalatexModule {

  def moduleDeps = Seq(sitegen)

  def scalatexSources = T.sources(
    millSourcePath / "src" / "scalatex"
  )

  def sources = T.sources(
    millSourcePath / "src" / "scala"
  )
}

// TODO use scalatex with JS as well
trait QASRLBrowserModule extends CommonModule with ScalaModule {

  def platformSegment: String

  def ivyDeps = super.ivyDeps() ++ Agg(
    // ivy"com.github.julien-truffaut::monocle-core::$monocleVersion",
    // ivy"com.github.julien-truffaut::monocle-macro::$monocleVersion",
    // ivy"com.github.julianmichael::qasrl::0.1-SNAPSHOT"
    // ivy"com.softwaremill.macmemo::macros:$macmemoVersion"
  )

  // def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
  //   ivy"org.scalamacros:::paradise:$macroParadiseVersion"
  // )

  def sourcePath = build.millSourcePath / "qasrlbrowser"

  def sources = T.sources(
    sourcePath / platformSegment / "src" / "scala",
    sourcePath / "shared" / "src" / "scala"
  )
}

// object qasrlbrowserJVM extends QASRLBrowserModule {
//   def platformSegment = "jvm"
// }

object qasrlbrowserJS extends QASRLBrowserModule with ScalaJSModule {
  def scalaJSVersion = thisScalaJSVersion
  def platformSegment = "js"

  // def scalaLibraryIvyDeps = super.scalaLibraryIvyDeps() ++ Agg(
  //   ivy"org.scala-lang:scala-library:${scalaVersion()}".forceVersion(),
  //   ivy"org.scala-lang:scala-reflect:${scalaVersion()}".forceVersion()
  // )

  def ivyDeps = super.ivyDeps() ++ Agg(
    // ivy"org.scala-js::scalajs-dom::$scalajsDomVersion",
    // ivy"be.doeraene::scalajs-jquery::$scalajsJqueryVersion",
    // ivy"com.github.japgolly.scalajs-react::core::$scalajsReactVersion",
    // ivy"com.github.japgolly.scalajs-react::ext-monocle::$scalajsReactVersion",
    // ivy"com.github.japgolly.scalajs-react::ext-cats::$scalajsReactVersion",
    // ivy"com.github.japgolly.scalacss::ext-react::$scalajsScalaCSSVersion"
  )

}
