import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import coursier.maven.MavenRepository
import ammonite.ops._

val thisScalaVersion = "2.12.6"
val scalatagsVersion = "0.6.7"
val catsVersion = "1.1.0"
val scalacssVersion = "0.5.3"

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

  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.lihaoyi::scalatags::$scalatagsVersion",
    ivy"org.typelevel::cats-core::$catsVersion",
    ivy"com.github.japgolly.scalacss::core:$scalacssVersion",
    ivy"com.github.japgolly.scalacss::ext-scalatags:$scalacssVersion"
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
