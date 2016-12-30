package org.softwaremill.clippy

import ammonite.repl.Highlighter
import org.scalatest.{FlatSpec, Matchers}

class HighlighterTest extends FlatSpec with Matchers {

  it should "work" in {
    val buffer = "val x: SomeType = 35".toVector

    val result = Highlighter.defaultHighlight(
      buffer,
      fansi.Color.LightGray,
      fansi.Color.Cyan,
      fansi.Color.LightGreen,
      fansi.Color.Yellow,
      fansi.Attr.Reset
    ).mkString

    println(result)
  }

}
