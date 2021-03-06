package com.softwaremill.clippy

object CompilationErrorParser {
  private val FoundRegexp = """found\s*:\s*([^\n]+)\n""".r
  private val RequiredPrefixRegexp = """required\s*:""".r
  private val AfterRequiredRegexp = """required\s*:\s*([^\n]+)""".r
  private val WhichExpandsToRegexp = """\s*\(which expands to\)\s*([^\n]+)""".r
  private val NotFoundRegexp = """not found\s*:\s*([^\n]+)""".r
  private val NotAMemberRegexp = """:?\s*([^\n:]+) is not a member of""".r
  private val NotAMemberOfRegexp = """is not a member of\s*([^\n]+)""".r
  private val ImplicitNotFoundRegexp = """could not find implicit value for parameter\s*([^:]+):\s*([^\n]+)""".r
  private val DivergingImplicitExpansionRegexp = """diverging implicit expansion for type\s*([^\s]+)\s*.*\s*starting with method\s*([^\s]+)\s*in\s*([^\n]+)""".r

  def parse(error: String): Option[CompilationError[ExactT]] = {
    if (error.contains("type mismatch")) {
      RequiredPrefixRegexp.split(error).toList match {
        case List(beforeReq, afterReq) =>
          for {
            found <- FoundRegexp.findFirstMatchIn(beforeReq)
            foundExpandsTo = WhichExpandsToRegexp.findFirstMatchIn(beforeReq)
            required <- AfterRequiredRegexp.findFirstMatchIn(error)
            requiredExpandsTo = WhichExpandsToRegexp.findFirstMatchIn(afterReq)
          } yield TypeMismatchError[ExactT](ExactT(found.group(1)), foundExpandsTo.map(m => ExactT(m.group(1))),
            ExactT(required.group(1)), requiredExpandsTo.map(m => ExactT(m.group(1))))

        case _ =>
          None
      }
    }
    else if (error.contains("not found")) {
      for {
        what <- NotFoundRegexp.findFirstMatchIn(error)
      } yield NotFoundError[ExactT](ExactT(what.group(1)))
    }
    else if (error.contains("is not a member of")) {
      for {
        what <- NotAMemberRegexp.findFirstMatchIn(error)
        notAMemberOf <- NotAMemberOfRegexp.findFirstMatchIn(error)
      } yield NotAMemberError[ExactT](ExactT(what.group(1)), ExactT(notAMemberOf.group(1)))
    }
    else if (error.contains("could not find implicit value for parameter")) {
      for {
        inf <- ImplicitNotFoundRegexp.findFirstMatchIn(error)
      } yield ImplicitNotFoundError[ExactT](ExactT(inf.group(1)), ExactT(inf.group(2)))
    }
    else if (error.contains("diverging implicit expansion for type")) {
      for {
        inf <- DivergingImplicitExpansionRegexp.findFirstMatchIn(error)
      } yield DivergingImplicitExpansionError[ExactT](ExactT(inf.group(1)), ExactT(inf.group(2)), ExactT(inf.group(3)))
    }
    else None
  }
}
