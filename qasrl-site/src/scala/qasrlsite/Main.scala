package qasrlsite

// import java.nio.file.StandardCopyOption
// import java.nio.file.Files
// import java.nio.file.Paths
import ammonite.ops._

import scalacss.DevDefaults._

import scalatags.Text.all.Frag

object Main extends App {

  // TODO generate data index JS file as part of thing

  val pathToBrowserJSOpt = Option(args(0)).filter(_.nonEmpty).map(Path(_))
  val pathToBrowserJSDepsOpt = args.lift(1).filter(_.nonEmpty).map(Path(_))
  val pathToDemoJSOpt = args.lift(2).filter(_.nonEmpty).map(Path(_))
  val pathToDemoJSDepsOpt = args.lift(3).filter(_.nonEmpty).map(Path(_))

  val siteRoot = pwd / "site" / "qasrl.org"
  val browserRoot = pwd / "site" / "browse.qasrl.org"
  val demoRoot = pwd / "site" / "demo.qasrl.org"

  val htmlFiles: Map[Frag, Path] = Map(
    Index() -> siteRoot / "index.html",
    Error() -> siteRoot / "error.html",
    Browser() -> browserRoot / "index.html",
    Error() -> browserRoot / "error.html",
    Demo() -> demoRoot / "index.html",
    Error() -> demoRoot / "error.html"
  )

  htmlFiles.foreach { case (html, path) =>
    write.over(path, "<!doctype html>\n" + html.render)
    System.out.println(s"Wrote ${path.relativeTo(pwd)}")
  }
  pathToBrowserJSOpt.filter(exists!).foreach { pathToBrowserJS =>
    cp.over(pathToBrowserJS, browserRoot / browserScriptLocation)
    System.out.println(
      s"Copied ${pathToBrowserJS.relativeTo(pwd)} " +
        s"to ${(browserRoot / browserScriptLocation).relativeTo(pwd)}"
    )
  }
  pathToBrowserJSDepsOpt.filter(exists!).foreach { pathToBrowserJSDeps =>
    cp.over(pathToBrowserJSDeps, browserRoot / browserScriptDepsLocation)
    System.out.println(
      s"Copied ${pathToBrowserJSDeps.relativeTo(pwd)} " +
        s"to ${(browserRoot / browserScriptDepsLocation).relativeTo(pwd)}"
    )
  }
  pathToDemoJSOpt.filter(exists!).foreach { pathToDemoJS =>
    cp.over(pathToDemoJS, demoRoot / demoScriptLocation)
    System.out.println(
      s"Copied ${pathToDemoJS.relativeTo(pwd)} " +
        s"to ${(demoRoot / demoScriptLocation).relativeTo(pwd)}"
    )
  }
  pathToDemoJSDepsOpt.filter(exists!).foreach { pathToDemoJSDeps =>
    cp.over(pathToDemoJSDeps, demoRoot / demoScriptDepsLocation)
    System.out.println(
      s"Copied ${pathToDemoJSDeps.relativeTo(pwd)} " +
        s"to ${(demoRoot / demoScriptDepsLocation).relativeTo(pwd)}"
    )
  }
}
