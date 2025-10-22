package hellosmithy4s

import cats.effect.*
import scribe.Scribe
import cats.syntax.all.*

import hellosmithy4s.spec.*
import skunk.SqlState
import org.typelevel.otel4s.trace.Tracer

class HelloImplementation(logger: Scribe[IO], database: Database)(using
    Tracer[IO]
) extends HelloService[IO]:

  override def health(): IO[HealthOutput] =
    HealthOutput("OK").pure[IO]

  override def add(name: Name): IO[Unit] =
    Tracer[IO].span("addname").surround(database.add(name))

  override def list(): IO[ListOutput] =
    database.list().map(ListOutput(_))
end HelloImplementation
