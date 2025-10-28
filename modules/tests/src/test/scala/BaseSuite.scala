package hellosmithy4s

import cats.effect.IO
import weaver.*
import cats.effect.std.Random

trait BaseSuite extends IOSuite:
  override type Res = Probe

  lazy val cls = this.getClass.getSimpleName()

  def named(n: String) = Random
    .scalaUtilRandom[IO]
    .flatMap(_.nextAlphaNumeric.replicateA(10).map(_.mkString))
    .map(cls + "-" + n + "-" + _)

  def probeTest(name: weaver.TestName)(f: Probe => IO[weaver.Expectations]) =
    test(name) { (probe, _) =>
      f(probe).attempt.flatMap(IO.fromEither)
    }
end BaseSuite
