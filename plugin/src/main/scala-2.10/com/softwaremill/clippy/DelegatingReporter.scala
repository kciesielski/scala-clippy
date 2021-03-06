package com.softwaremill.clippy

import scala.reflect.internal.util.Position
import scala.tools.nsc.reporters.Reporter

class DelegatingReporter(r: Reporter, handleError: (Position, String) => String) extends Reporter {
  override protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean) = {
    // cannot delegate to info0 as it's protected, hence special-casing on the possible severity values
    if (severity == INFO) {
      info(pos, msg, force)
    } else if (severity == WARNING) {
      warning(pos, msg)
    } else if (severity == ERROR) {
      error(pos, msg)
    } else {
      error(pos, s"UNKNOWN SEVERITY: $severity\n$msg")
    }
  }

  override def echo(msg: String) = r.echo(msg)
  override def comment(pos: Position, msg: String) = r.comment(pos, msg)
  override def hasErrors = r.hasErrors || cancelled
  override def reset() = {
    cancelled = false
    r.reset()
  }

  //

  override def echo(pos: Position, msg: String) = r.echo(pos, msg)
  override def warning(pos: Position, msg: String) = r.warning(pos, msg)
  override def hasWarnings = r.hasWarnings
  override def flush() = r.flush()

  //

  private def conv(s: Severity): r.Severity = s match {
    case INFO => r.INFO
    case WARNING => r.WARNING
    case ERROR => r.ERROR
  }

  //

  override def error(pos: Position, msg: String) = r.error(pos, handleError(pos, msg))
}
