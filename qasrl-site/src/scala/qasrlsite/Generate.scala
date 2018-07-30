package qasrlsite

import java.nio.file.Path
import java.nio.file.Files

import cats.effect.IO
import cats.implicits._

import scalacss.DevDefaults._

import scalatags.Text.all.Frag

import com.monovore.decline._

object Generate {

  def main(args: Array[String]): Unit = {
    val command = Command(
      name = "mill `qasrl-site`.jvm.run",
      header = "Generate the static QA-SRL website."
    ) {
      val siteRoot = Opts.option[Path](
        "site-root", metavar = "path", help = "Root directory in which to place the generated website."
      )
      val useLocalLinks = Opts.flag(
        "local-links", help = "Use links to site-local versions of Bootstrap dependencies"
      ).orFalse

      (siteRoot, useLocalLinks).mapN(program)
    }
    val result = command.parse(args) match {
      case Left(help) => IO { System.err.println(help) }
      case Right(main) => main
    }
    result.unsafeRunSync
  }

  def program(
    siteRoot: Path,
    useLocalLinks: Boolean
  ): IO[Unit] = {

    import sitegen.render._

    val links: Links = if(useLocalLinks) LocalLinks else CDNLinks

    val htmlFiles: Map[Frag, Path] = Map(
      Index(links) -> siteRoot.resolve("index.html"),
      Error(links) -> siteRoot.resolve("error.html")
    )

    IO {
      def writeFile(path: Path, content: String): Unit = {
        Files.createDirectories(path.getParent)
        Files.write(path, content.getBytes("utf-8"))
      }

      htmlFiles.foreach { case (html, path) =>
        writeFile(path, "<!doctype html>\n" + html.render)
        System.out.println(s"Wrote $path")
      }
    }
  }
}
