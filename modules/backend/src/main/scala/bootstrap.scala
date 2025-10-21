package hellosmithy4s

import cats.effect.*
import cats.syntax.all.*
import org.http4s.server.Server
import java.io.File
import org.typelevel.otel4s.trace.Tracer
import com.monovore.decline.CommandApp
import decline_derive.CommandApplication

def bootstrap(
    arguments: List[String],
    systemEnv: Map[String, String]
): Resource[IO, Server] =
  val logger = scribe.cats.io
  import fs2.io.file.*
  val dotEnv = Path(".env")

  val opts = fs2.io.file.Files.forIO
    .exists(dotEnv)
    .flatMap:
      case false => IO.pure(Map.empty)
      case true  =>
        Log.info(s"Loading env from $dotEnv") *>
          DotEnvLoader.load(dotEnv.toNioPath.toFile).handleErrorWith { err =>
            logger
              .error(
                s"Failed to load properties from file ${dotEnv}",
                err
              )
              .as(Map.empty)
          }

  val allEnv = opts.map { fallback =>
    (systemEnv.keySet ++ fallback.keySet).flatMap { key =>
      val value = systemEnv.get(key).orElse(fallback.get(key))

      value.map(v => key -> v)
    }.toMap
  }

  for
    env <- Resource.eval(allEnv)

    given Tracer[IO] = Tracer.Implicits.noop[IO]

    cli <- IO(CommandApplication.parseOrExit[CLI](arguments, env)).toResource

    pgCredentials = PgCredentials.fromEnv(env)

    db <- SkunkDatabase.make(pgCredentials, SkunkConfig)

    services = Services.build(
      logger,
      db
    )

    routes <- Routes.build(
      services
    )

    _ <- migrate(pgCredentials).toResource

    server <- Server(cli.http, routes)
  yield server
  end for
end bootstrap
