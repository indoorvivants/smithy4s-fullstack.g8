package hellosmithy4s

import hellosmithy4s.spec.HelloService
import cats.effect.IO
import scribe.Scribe
import org.typelevel.otel4s.trace.Tracer

case class Services(hello: HelloService[IO])

object Services:
  def build(logger: Scribe[IO], database: Database)(using Tracer[IO]) =
    Services(HelloImplementation(logger, database))
