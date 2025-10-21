package hellosmithy4s

import cats.effect.*
import scribe.Scribe
import cats.syntax.all.*

import hellosmithy4s.spec.*
import skunk.SqlState

class HelloImplementation(logger: Scribe[IO], database: Database)
    extends HelloService[IO]:

  override def health(): IO[HealthOutput] =
    HealthOutput("OK").pure[IO]

  override def add(name: Name): IO[Unit] =
    database.add(name)

  override def list(): IO[ListOutput] =
    database.list().map(ListOutput(_))
end HelloImplementation
