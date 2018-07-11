package qasrlbrowser

case class Rgba(r: Double, g: Double, b: Double, a: Double) {
  def add(that: Rgba) = {
    if(this.a == 0.0) that else if(that.a == 0.0) this else {
      val alpha = 1.0 - ((1.0 - a) * (1.0 - that.a))
      Rgba(
        (a * r / alpha) + ((1.0 - a) * (that.r * that.a) / alpha),
        (a * g / alpha) + ((1.0 - a) * (that.g * that.a) / alpha),
        (a * b / alpha) + ((1.0 - a) * (that.b * that.a) / alpha),
        alpha
      )
    }
  }
  def toColorStyleString = f"rgba(${math.round(r)}%d, ${math.round(g)}%d, ${math.round(b)}%d, $a%.4f)"
}
// val reddish = Rgba(255, 0, 0, 0.1)
// val greenish = Rgba(0, 255, 0, 0.1)
// val yellow = Rgba(255, 255, 0, 0.1)
// def flatten(colors: List[Rgba]) = colors.reduce(_ over _)

// def makeColorSampleDiv(colors: List[Rgba]) = <.div(
//   ^.style := {
//     js.Dynamic.literal("background-color" -> flatten(colors).toColorStyleString)
//   },
//   "MMMMMM"
// )
