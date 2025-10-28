package hellosmithy4s

import cats.effect.*
import skunk.*, skunk.syntax.all.*, codec.all.*
import skunk.data.Completion
import cats.syntax.all.*
import org.typelevel.otel4s.trace.Tracer
import hellosmithy4s.spec.Name
import smithy4s.Newtype
import hellosmithy4s.spec.Item
import hellosmithy4s.spec.NameAlreadyExists

class SkunkDatabase private (makeSession: Resource[IO, Session[IO]])
    extends Database,
      SkunkDatabase.Helpers(makeSession):
  override def list() =
    transact(_.execute(sql"select name from examples".query(itemCodec)))

  override def add(name: Name) =
    transact(
      _.execute(sql"insert into examples(name) values (${nameCodec})".command)(
        name
      )
    ).void
      .recoverWith:
        // mixing logic and concrete database classes is frankly no bueno
        case SqlState.UniqueViolation(ex) =>
          IO.raiseError(NameAlreadyExists())

  private val defaultChunkSize = 512

  def newtypeCodec[A](nt: Newtype[A], underlying: Codec[A]): Codec[nt.Type] =
    underlying.imap(nt.apply)(_.value)
  val nameCodec = newtypeCodec(Name, varchar(50))
  val itemCodec = nameCodec.gimap[Item]

end SkunkDatabase

object SkunkDatabase:
  def make(creds: PgCredentials, config: SkunkConfig)(using Tracer[IO]) =
    Session
      .pooled[IO](
        host = creds.host,
        port = creds.port,
        user = creds.user,
        password = creds.password,
        database = creds.database,
        // TODO double-check if this is equivalent with the hikari setup
        max = config.maxSessions
      )
      .map(SkunkDatabase(_))

  trait Helpers(makeSession: Resource[IO, Session[IO]]):

    protected val transactionalSession = for
      session     <- makeSession
      transaction <- session.transaction
    yield (session, transaction)

    protected def transact[A](
        body: Session[IO] => fs2.Stream[IO, A]
    ): fs2.Stream[IO, A] =
      fs2.Stream.resource(transactionalSession).flatMap { (session, _) =>
        body(session)
      }

    protected def transact[A](body: Session[IO] => IO[A]): IO[A] =
      transactionalSession.use { (session, _) =>
        body(session)
      }

    protected def dmlAffectedCount(
        completion: Completion,
        command: Command[?]
    ): IO[Int] = completion match
      case Completion.Update(count) => count.pure[IO]
      case Completion.Insert(count) => count.pure[IO]
      case Completion.Delete(count) => count.pure[IO]
      case x                        =>
        IO.raiseError(
          new RuntimeException(
            s"Unexpected completion $x, SQL was ${command.sql}"
          )
        )
  end Helpers

end SkunkDatabase

