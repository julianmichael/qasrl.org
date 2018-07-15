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

  def runFn = T.task { (args: Seq[String]) =>
    import mill.modules.Jvm
    import mill.eval.Result
    try Result.Success(Jvm.interactiveSubprocess(
                         finalMainClass(),
                         runClasspath().map(_.path),
                         forkArgs(),
                         forkEnv(),
                         args,
                         workingDir = ammonite.ops.pwd
                       )) catch { case e: InteractiveShelloutException =>
        Result.Failure("subprocess failed")
    }
  }

  def generate() = T.command {
    val browserJSPath = `qasrl-browser`.js.fastOpt().path.toString
    val browserJSDepsPath = `qasrl-browser`.js.aggregatedJSDeps().path.toString
    val run = runFn()
    run(Seq(browserJSPath, browserJSDepsPath))
  }
}

// trait QasrlServiceModule extends CommonMainModule with CrossPlatformModule {

//   def scalacOptions = super.scalacOptions() ++ Seq(
//     "-language:higherKinds"
//   )

//   def millSourcePath = build.millSourcePath / "qasrl-service"

//   def ivyDeps = super.ivyDeps() ++ Agg(
//     ivy"org.julianmichael::qasrl::$qasrlVersion",
//     ivy"org.typelevel::cats-core::$catsVersion",
//     ivy"io.circe::circe-core::$circeVersion",
//     ivy"io.circe::circe-generic::$circeVersion",
//     ivy"io.circe::circe-parser::$circeVersion"
//     // ivy"org.http4s::http4s-core::$http4sVersion"
//   )

//   trait QasrlServiceTestModule extends Tests with CommonModule with CrossPlatformModule {
//     def platformSegment = QasrlServiceModule.this.platformSegment
//     def ivyDeps = Agg(
//       ivy"org.scalatest::scalatest:3.0.1"
//       // ivy"org.scalacheck::scalacheck:1.13.4",
//       // ivy"org.typelevel::discipline:0.7.3"
//     )
//     def testFrameworks = Seq("org.scalatest.tools.Framework")
//   }

// }
// object `qasrl-service` extends Module {
//   object jvm extends QasrlServiceModule with JvmPlatform {
//     def ivyDeps = super.ivyDeps() ++ Agg(
//       ivy"org.http4s::http4s-core::$http4sVersion"
//     )
//     object test extends QasrlServiceTestModule
//   }
//   object js extends QasrlServiceModule with JsPlatform {
//     object test extends QasrlServiceTestModule
//   }
// }

trait QASRLBrowserModule extends CommonMainModule {
  def millSourcePath = build.millSourcePath / "qasrl-browser"
}

import $file.lib.sitegen.scripts.SimpleJSDepsBuild

object `qasrl-browser` extends Module {
  object jvm extends QASRLBrowserModule with ScalatexBuild.ScalatexModule with JvmPlatform {
    // def moduleDeps = super.moduleDeps() ++ Seq(
    //   `qasrl-bank`.jvm,
    //   `qasrl-service`.jvm
    // )
  }
  object js extends QASRLBrowserModule with JsPlatform with SimpleJSDepsBuild.SimpleJSDeps /* with ScalatexReactJSModule */ {
    def moduleDeps = Seq(
      `qasrl-bank`.js
    )

    def mainClass = T(Some("qasrlbrowser.Main"))

    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.julianmichael::qasrl::$qasrlVersion",
      ivy"org.julianmichael::nlpdata::$nlpdataVersion",
      ivy"com.github.julien-truffaut::monocle-core::$monocleVersion",
      ivy"com.github.julien-truffaut::monocle-macro::$monocleVersion",
      ivy"org.scala-js::scalajs-dom::$scalajsDomVersion",
      ivy"com.github.japgolly.scalajs-react::core::$scalajsReactVersion",
      ivy"com.github.japgolly.scalajs-react::ext-monocle::$scalajsReactVersion",
      ivy"com.github.japgolly.scalajs-react::ext-cats::$scalajsReactVersion",
      ivy"com.github.japgolly.scalacss::ext-react::$scalajsScalaCSSVersion"
    )

    // def jsDeps = Agg(
    //   "https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react.js",
    //   "https://cdnjs.cloudflare.com/ajax/libs/react/15.6.1/react-dom.js"
    // )
  }
}

trait QasrlBankModule extends CommonMainModule {

  def millSourcePath = build.millSourcePath / "qasrl-bank"

  def ivyDeps = super.ivyDeps() ++ Agg(
    ivy"org.julianmichael::qasrl::$qasrlVersion",
    ivy"org.typelevel::cats-free::$catsVersion",
    ivy"io.circe::circe-core::$circeVersion",
    ivy"io.circe::circe-generic::$circeVersion"
  )
}

object `qasrl-bank` extends Module {
  object jvm extends QasrlBankModule with JvmPlatform {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.http4s::http4s-dsl::$http4sVersion",
      ivy"org.http4s::http4s-blaze-server::$http4sVersion",
      ivy"org.http4s::http4s-circe::$http4sVersion"
      // Optional for string interpolation to JSON model
      // "io.circe" %% "circe-literal" % "0.9.3"
    )
  }
  object js extends QasrlBankModule with JsPlatform {
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"io.circe::circe-parser::$circeVersion",
      ivy"org.scala-js::scalajs-dom::$scalajsDomVersion"
    )
  }
}
