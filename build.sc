import mill._, mill.scalalib._, mill.scalalib.publish._, mill.scalajslib._
import mill.api.DummyInputStream
import mill.eval.Result
import coursier.maven.MavenRepository
import ammonite.ops._

val thisScalaVersion = "2.12.8"
val thisScalaJSVersion = "0.6.27"

val macroParadiseVersion = "2.1.0"
val kindProjectorVersion = "0.9.4"

// cats libs -- maintain version agreement or whatever
val jjmVersion = "0.1.0-SNAPSHOT"
val qasrlVersion = "0.2.0-SNAPSHOT"
val qasrlBankVersion = "0.2.0-SNAPSHOT"
val radhocVersion = "0.3.0-SNAPSHOT"
val declineVersion = "1.0.0"

val scalatagsVersion = "0.6.7"
val scalacssVersion = "0.5.3"

val scalajsDomVersion = "0.9.6"
val scalajsJqueryVersion = "0.9.3"
// val scalajsScalaCSSVersion = "0.5.3"

// workaround for source project dependency
import $file.lib.sitegen.{build => SitegenBuild}
object sitegen extends SitegenBuild.SitegenModule {
  def millSourcePath = build.millSourcePath / "lib" / "sitegen" / "sitegen"
}

trait CommonModule extends ScalaModule {
  override def scalacOptions = Seq(
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

  def runMainFn = T.task { (mainClass: String, args: Seq[String]) =>
    import mill.modules.Jvm
    import mill.eval.Result
    try Result.Success(
      Jvm.runSubprocess(
        mainClass,
        runClasspath().map(_.path),
        forkArgs(),
        forkEnv(),
        args,
        workingDir = ammonite.ops.pwd
      )
    ) catch {
      case e: InteractiveShelloutException =>
        Result.Failure("subprocess failed")
    }
  }
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

import $file.lib.`qasrl-apps`.{build => Apps}
object apps extends Apps.Build {
  def buildRoot = build.millSourcePath / "lib" / "qasrl-apps"
}

import $file.lib.sitegen.scripts.ScalatexBuild
object `qasrl-site` extends CommonMainModule with ScalatexBuild.ScalatexModule {

  def moduleDeps = Seq(sitegen.jvm)

  override def ivyDeps = super.ivyDeps() ++ Agg(
    // ivy"org.typelevel::cats-effect::$catsEffectVersion",
    ivy"com.monovore::decline::$declineVersion",
    ivy"com.monovore::decline-effect::$declineVersion"
  )

  def scalatexSources = T.sources(
    millSourcePath / "src" / "scalatex"
  )

  def sources = T.sources(
    millSourcePath / "src" / "scala"
  )
}

object tasks extends Module {

  implicit val wd = build.millSourcePath

  val siteRoot = build.millSourcePath / "site"

  val devMainSiteRoot     = siteRoot / "dev"  / "qasrl.org"
  val prodMainSiteRoot    = siteRoot / "prod" / "qasrl.org"

  def qasrlBankDataPath = T.input(
    PathRef(prodMainSiteRoot / "data")
  )

  def qasrlBankDownload = T {
    val dataPath = qasrlBankDataPath().path
    val tarPath =  dataPath / "qasrl-v2.tar"
    if(!exists(tarPath)) {
      mkdir! dataPath
      System.out.println("Downloading the QA-SRL Bank 2.0...")
      %("curl", "-o", tarPath, "http://qasrl.org/data/qasrl-v2.tar")
      %("tar",  "xf", tarPath, "-C", dataPath)
    }
    PathRef(dataPath / "qasrl-v2")
  }

  val devBrowserPort  = 8001
  val devBrowserRoot  = siteRoot / "dev" / "browse.qasrl.org"

  val prodBrowserPort = 8011
  val prodBrowserRoot = siteRoot / "prod" / "browse.qasrl.org"
  val prodBrowserApiDomain = "recycle.cs.washington.edu"
  val prodBrowserPageUrlPrefix = "http://browse.qasrl.org"

  val devDemoRoot   = siteRoot / "dev" / "demo.qasrl.org"
  val devDemoApiUrl = "http://localhost:5000/predict"

  val prodDemoRoot   = siteRoot / "prod" / "demo.qasrl.org"
  val prodDemoApiUrl = "http://recycle.cs.washington.edu:5000/predict"

  object dev extends Module {
    object site extends Module {
      def gen() = T.command {
        val runMain = `qasrl-site`.runMainFn()
        runMain(
          "qasrl.site.Generate", Seq(
            "--site-root",       devMainSiteRoot.toString,
            "--local-links"
          )
        )
      }
    }
    object browser extends Module {
      def gen() = T.command {
        val browserJSPath = apps.browser.js.fastOpt().path
        val browserJSDepsPath = apps.browser.js.aggregatedJSDeps().path
        val runMain = apps.browser.jvm.runMainFn()
        runMain(
          "qasrl.apps.browser.Generate", Seq(
            "--qasrl-bank",      qasrlBankDownload().path.toString,
            "--api-url",         s"http://localhost:$devBrowserPort",
            "--browser-js",      browserJSPath.toString,
            "--browser-jsdeps",  browserJSDepsPath.toString,
            "--site-root",       devBrowserRoot.toString,
            "--local-links"
          )
        )
      }
      def serve() = T.command {
        if (T.ctx().log.inStream == DummyInputStream){
          Result.Failure("server needs to be run with the -i/--interactive flag")
        } else {
          val runMain = apps.browser.jvm.runMainFn()
          runMain(
            "qasrl.apps.browser.Serve", Seq(
              "--qasrl-bank", qasrlBankDownload().path.toString,
              "--port",       s"$devBrowserPort"
            )
          )
        }
      }
    }
    object demo extends Module {
      def gen() = T.command {
        val demoJSPath = apps.demo.js.fastOpt().path.toString
        val demoJSDepsPath = apps.demo.js.aggregatedJSDeps().path.toString
        val runMain = apps.demo.jvm.runMainFn()
        runMain(
          "qasrl.apps.demo.Generate", Seq(
            "--api-url",      devDemoApiUrl,
            "--demo-js",      demoJSPath,
            "--demo-jsdeps",  demoJSDepsPath,
            "--site-root",    devDemoRoot.toString,
            "--local-links"
          )
        )
      }
    }
  }

  def s3Sync(
    directory: Path,
    bucketUrl: String
  ) = %(
    "aws", "s3", "sync", directory.toString, bucketUrl,
    "--exclude", "**/.DS_Store",
    "--exclude", "**/.gitignore",
    "--profile", "cse-julian"
  )

  object prod extends Module {
    object site extends Module {
      def gen() = T.command {
        val runMain = `qasrl-site`.runMainFn()
        runMain(
          "qasrl.site.Generate", Seq(
            "--site-root",       prodMainSiteRoot.toString
          )
        )
      }
      def deploy() = T.command {
        s3Sync(prodMainSiteRoot, "s3://qasrl.org")
      }
    }
    object browser extends Module {
      def gen() = T.command {
        val browserJSPath = apps.browser.js.fullOpt().path
        val browserJSDepsPath = apps.browser.js.aggregatedJSDeps().path
        val runMain = apps.browser.jvm.runMainFn()
        runMain(
          "qasrl.apps.browser.Generate", Seq(
            "--qasrl-bank",      qasrlBankDownload().path.toString,
            "--api-url",         s"http://$prodBrowserApiDomain:$prodBrowserPort",
            "--browser-js",      browserJSPath.toString,
            "--browser-jsdeps",  browserJSDepsPath.toString,
            "--site-root",       prodBrowserRoot.toString
          )
        )
      }
      def serve() = T.command {
        if (T.ctx().log.inStream == DummyInputStream){
          Result.Failure("server needs to be run with the -i/--interactive flag")
        } else {
          val runMain = apps.browser.jvm.runMainFn()
          runMain(
            "qasrl.apps.browser.Serve", Seq(
              "--qasrl-bank", qasrlBankDownload().path.toString,
              "--port",       s"$prodBrowserPort",
              "--domain",     prodBrowserPageUrlPrefix
            )
          )
        }
      }
      def deploy() = T.command {
        s3Sync(prodBrowserRoot, "s3://browse.qasrl.org")
      }
    }
    object demo extends Module {
      def gen() = T.command {
        val demoJSPath = apps.demo.js.fullOpt().path.toString
        val demoJSDepsPath = apps.demo.js.aggregatedJSDeps().path.toString
        val runMain = apps.demo.jvm.runMainFn()
        runMain(
          "qasrl.apps.demo.Generate", Seq(
            "--api-url",      prodDemoApiUrl,
            "--demo-js",      demoJSPath,
            "--demo-jsdeps",  demoJSDepsPath,
            "--site-root",    prodDemoRoot.toString
          )
        )
      }
      def deploy() = T.command {
        s3Sync(prodDemoRoot, "s3://demo.qasrl.org")
      }
    }
  }
}
