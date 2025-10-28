package hellosmithy4s

import cats.data.Kleisli
import cats.effect.*
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.implicits.*
import smithy4s.http4s.SimpleRestJsonBuilder

object Routes:
  def build(
      app: Services,
      extra: HttpRoutes[IO]*
  ) =
    def handleErrors(routes: HttpRoutes[IO]) =
      routes.orNotFound.onError { exc =>
        Kleisli(request => Log.error(s"Request failed: [$request]", exc))
      }

    SimpleRestJsonBuilder
      .routes(app.hello)
      .resource
      .map: serviceRoutes =>
        handleErrors(
          (serviceRoutes :: extra.toList)
            .reduce(_ <+> _)
        )
  end build
end Routes
