package hellosmithy4s

import cats.effect.IO
import hellosmithy4s.spec.HelloService
import org.typelevel.otel4s.trace.Tracer
import scribe.Scribe

case class Services(hello: HelloService[IO])

object Services:
  def build(logger: Scribe[IO], database: Database)(using Tracer[IO]) =
    Services(HelloServiceImplementation(logger, database))
