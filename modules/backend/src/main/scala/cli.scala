package hellosmithy4s

import cats.data.ValidatedNel
import cats.syntax.all.*
import com.comcast.ip4s.*
import com.monovore.decline.Argument
import decline_derive.*

case class HttpConfig(port: Port = port"8080", host: Host = host"localhost")
    derives CommandApplication

case class CLI(
    http: HttpConfig
) derives CommandApplication

extension [A, B](x: Argument[A])
  private def mapValidated(f: A => ValidatedNel[String, B]): Argument[B] =
    new Argument[B]:
      override def read(string: String): ValidatedNel[String, B] =
        x.read(string).andThen(f)

      override def defaultMetavar: String = x.defaultMetavar

private given Argument[Port] =
  Argument.readInt.mapValidated(p =>
    Port.fromInt(p).toRight(s"Invalid port $p").toValidatedNel
  )

private given Argument[Host] =
  Argument.readString.mapValidated(p =>
    Host.fromString(p).toRight(s"Invalid host $p").toValidatedNel
  )
