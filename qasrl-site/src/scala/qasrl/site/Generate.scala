package qasrl.site

import java.nio.file.Path
import java.nio.file.Paths
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

    sealed trait LinkType; case object CSSLink extends LinkType; case object JSLink extends LinkType
    case class LinkForDownload(
      remoteUrl: String,
      localLocation: String,
      linkType: LinkType
    ) {
      import scalatags.Text.all._
      def makeTag(isLocal: Boolean) = linkType match {
        case CSSLink => link(
          rel := "stylesheet",
          href := (if(isLocal) localLocation else remoteUrl)
        )
        case JSLink => script(
          src := (if(isLocal) localLocation else remoteUrl)
        )
      }
    }

    val (bootstrapLink, bootstrapScripts, clipboardScript) = {
      val bootstrapLink = LinkForDownload(
        "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css",
        "css/bootstrap.min.css",
        CSSLink
      )
      val scriptLinks = List(
        LinkForDownload(
          "https://code.jquery.com/jquery-3.2.1.slim.min.js",
          "scripts/jquery-3.2.1.slim.min.js",
          JSLink
        ),
        LinkForDownload(
          "https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js",
          "scripts/popper.min.js",
          JSLink
        ),
        LinkForDownload(
          "https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js",
          "scripts/bootstrap.min.js",
          JSLink
        ),
      )
      val clipboardScript = LinkForDownload(
        "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/2.0.0/clipboard.min.js",
        "scripts/clipboard.min.js",
        JSLink
      )
      (
        bootstrapLink,
        scriptLinks,
        clipboardScript
      )
    }

    val config = {
      import scalatags.Text.all._
      GenerationConfig(
        bootstrapLink = bootstrapLink.makeTag(useLocalLinks),
        bootstrapScripts = div(bootstrapScripts.map(_.makeTag(useLocalLinks))),
        clipboardScripts = div(
          clipboardScript.makeTag(useLocalLinks),
          script(raw(sitegen.render.clipboardBibScript))
        ),
        siteRoot = siteRoot
      )
    }

    val htmlFiles: Map[Frag, Path] = Map(
      pages.Index(config) -> siteRoot.resolve("index.html"),
      pages.Error(config) -> siteRoot.resolve("error.html")
    )

    val assetFolders: Map[Path, Path] = Map(
      Paths.get("assets/images") -> siteRoot.resolve("images")
    )

    IO {
      def writeFile(path: Path, content: String): Unit = {
        Files.createDirectories(path.getParent)
        Files.write(path, content.getBytes("utf-8"))
      }

      def downloadFileToSite(url: String, location: String) = {
        val targetPath = siteRoot.resolve(location)
        if(!Files.exists(targetPath)) {
          Files.createDirectories(targetPath.getParent)
          import sys.process._
          s"curl -o $targetPath $url".!
        }
      }

      if(useLocalLinks) {
        (clipboardScript :: bootstrapLink :: bootstrapScripts).foreach { link =>
          downloadFileToSite(link.remoteUrl, link.localLocation)
        }
      }

      assetFolders.foreach { case (origin, target) =>
        import sys.process._
        Files.createDirectories(target)
        Seq("/bin/sh", "-c", s"cp -r $origin/* $target/").!
        System.out.println(s"Copied $origin to $target")
      }

      htmlFiles.foreach { case (html, path) =>
        writeFile(path, "<!doctype html>\n" + html.render)
        System.out.println(s"Wrote $path")
      }
    }
  }
}
