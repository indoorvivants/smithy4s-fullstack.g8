package hellosmithy4s
package stub

import cats.effect.Resource
import org.typelevel.otel4s.trace.Tracer

def buildApp: Resource[cats.effect.IO, Probe] =
  for
    db <- Resource.eval(InMemoryDatabase.create)
    logger = scribe.cats.io
    probe <- Probe.build(logger, db)(using Tracer.Implicits.noop)
    _     <- Routes.build(probe.api)
  yield probe
