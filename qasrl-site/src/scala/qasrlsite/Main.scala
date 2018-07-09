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

  val siteRoot = pwd / "site"

  val htmlFiles: Map[Frag, Path] = Map(
    Index() -> siteRoot / "index.html",
    Error() -> siteRoot / "error.html",
    Browser() -> siteRoot / "browser.html",
  )

  htmlFiles.foreach { case (html, path) =>
    write.over(path, "<!doctype html>\n" + html.render)
    System.out.println(s"Wrote ${path.relativeTo(pwd)}")
  }
  pathToBrowserJSOpt.filter(exists!).foreach { pathToBrowserJS =>
    cp.over(pathToBrowserJS, siteRoot / browserScriptLocation)
    System.out.println(
      s"Copied ${pathToBrowserJS.relativeTo(pwd)} " +
        s"to ${(siteRoot / browserScriptLocation).relativeTo(pwd)}"
    )
  }
  pathToBrowserJSDepsOpt.filter(exists!).foreach { pathToBrowserJSDeps =>
    cp.over(pathToBrowserJSDeps, siteRoot / browserScriptDepsLocation)
    System.out.println(
      s"Copied ${pathToBrowserJSDeps.relativeTo(pwd)} " +
        s"to ${(siteRoot / browserScriptDepsLocation).relativeTo(pwd)}"
    )
  }
}
