package hellosmithy4s

import cats.effect.*
import decline_derive.CommandApplication
import org.http4s.server.Server
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

/** This method bootstraps a HTTP server from CLI arguments, environment
  * variables, and from .env file if it's present at the root of working
  * directory
  *
  * @param arguments
  * @param systemEnv
  * @return
  */
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
    env    <- Resource.eval(allEnv)
    otel   <- OtelJava.autoConfigured[IO]()
    tracer <- otel.tracerProvider.get("org.hellosmithy4s").toResource
    given Tracer[IO] = tracer
    cli <- IO(CommandApplication.parseOrExit[CLI](arguments, env)).toResource
    pgCredentials = PgCredentials.fromEnv(env)
    db <- SkunkDatabase.make(pgCredentials, SkunkConfig)
    services = Services.build(logger, db)
    routes <- Routes.build(services)
    _      <- migrate(pgCredentials).toResource

    server <- Server(cli.http, routes)
  yield server
  end for
end bootstrap
