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

case class SkunkConfig(
    maxSessions: Int,
    strategy: skunk.TypingStrategy,
    debug: Boolean
)

object SkunkConfig
    extends SkunkConfig(
      maxSessions = 2,
      strategy = skunk.TypingStrategy.SearchPath,
      debug = false
    )
