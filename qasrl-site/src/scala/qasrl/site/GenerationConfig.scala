package qasrl.site

import java.nio.file.Path
import scalatags.Text.all.Frag

case class GenerationConfig(
  bootstrapLink: Frag,
  bootstrapScripts: Frag,
  clipboardScripts: Frag,
  siteRoot: Path
)
