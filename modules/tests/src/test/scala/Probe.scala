package hellosmithy4s

import cats.effect.IO
import cats.effect.kernel.Resource
import hellosmithy4s.Database
import hellosmithy4s.Services
import hellosmithy4s.spec.HelloService
import scribe.Scribe
import weaver.*
import org.typelevel.otel4s.trace.Tracer

case class Probe(api: Services)

object Probe:
  def build(logger: Scribe[IO], database: Database)(using Tracer[IO]) =
    Resource.eval(IO(Probe(Services.build(logger, database))))
