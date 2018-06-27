package qasrlsite

import java.nio.file.Files
import java.nio.file.Paths

import scalacss.DevDefaults._
import sitegen.render.Styles

import scalatags.Text.all.Frag

object Main extends App {
  val root = Paths.get("site")

  val htmlFiles: Map[Frag, String] = Map(
    Index() -> "index.html",
    Error() -> "error.html"
  )

  htmlFiles.foreach { case (html, pathStr) =>
    Files.write(root.resolve(pathStr), ("<!doctype html>\n" + html.render).getBytes("utf-8"))
  }
}
