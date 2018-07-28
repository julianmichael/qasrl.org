import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import coursier.maven.MavenRepository
import ammonite.ops._

val thisScalaVersion = "2.12.6"
val thisScalaJSVersion = "0.6.23"

val macroParadiseVersion = "2.1.0"
val kindProjectorVersion = "0.9.4"

// cats libs -- maintain version agreement or whatever
val catsVersion = "1.1.0"
val nlpdataVersion = "0.2.0-SNAPSHOT"
val qasrlVersion = "0.1.0-SNAPSHOT"
val qasrlBankVersion = "0.1.0-SNAPSHOT"
val radhocVersion = "0.1.0-SNAPSHOT"
val circeVersion = "0.9.3"
val http4sVersion = "0.18.14"

val scalatagsVersion = "0.6.7"
val scalacssVersion = "0.5.3"
val monocleVersion = "1.4.0"

val scalajsDomVersion = "0.9.6"
val scalajsJqueryVersion = "0.9.3"
val scalajsReactVersion = "1.1.0"
val scalajsScalaCSSVersion = "0.5.3"

// workaround for source project dependency
import $file.lib.sitegen.{build => SitegenBuild}
object sitegen extends SitegenBuild.SitegenModule {
  def millSourcePath = build.millSourcePath / "lib" / "sitegen" / "sitegen"
}

trait CommonModule extends ScalaModule {
  def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:higherKinds",
    "-Ypartial-unification"
  )
  def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(
    ivy"org.scalamacros:::paradise:$macroParadiseVersion",
    ivy"org.spire-math::kind-projector:$kindProjectorVersion"
  )
  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
  )
}

trait CommonMainModule extends CommonModule {
  def scalaVersion = thisScalaVersion
}

trait CrossPlatformModule extends ScalaModule {
  def platformSegment: String

  def sources = T.sources(
    millSourcePath / "src",
    millSourcePath / s"src-$platformSegment"
  )

}

trait JvmPlatform extends CrossPlatformModule {
  def platformSegment = "jvm"
}

trait JsPlatform extends CrossPlatformModule with ScalaJSModule {
  def scalaJSVersion = T(thisScalaJSVersion)
  def platformSegment = "js"
}

import $file.lib.sitegen.scripts.ScalatexBuild
object `qasrl-site` extends CommonMainModule with ScalatexBuild.ScalatexModule {

  def moduleDeps = Seq(sitegen.jvm)

  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"com.lihaoyi::ammonite-ops:1.1.2"
  )

  def scalatexSources = T.sources(
    millSourcePath / "src" / "scalatex"
  )

  def sources = T.sources(
    millSourcePath / "src" / "scala"
  )

  def generate() = T.command {
    run()
  }
}
