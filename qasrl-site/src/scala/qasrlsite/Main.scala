package qasrlsite

// import java.nio.file.StandardCopyOption
// import java.nio.file.Files
// import java.nio.file.Paths
import ammonite.ops._

import scalacss.DevDefaults._

import scalatags.Text.all.Frag

object Main extends App {

  val pathToBrowserJSOpt = Option(args(0)).filter(_.nonEmpty).map(Path(_))
  val pathToBrowserJSDepsOpt = Option(args(1)).filter(_.nonEmpty).map(Path(_))

  val siteRoot = pwd / "site" / "qasrl.org"
  val browserRoot = pwd / "site" / "browse.qasrl.org"

  val htmlFiles: Map[Frag, Path] = Map(
    Index() -> siteRoot / "index.html",
    Error() -> siteRoot / "error.html",
    Browser() -> browserRoot / "index.html",
    Error() -> browserRoot / "error.html"
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
}
