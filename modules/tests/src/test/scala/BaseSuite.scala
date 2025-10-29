package hellosmithy4s

import cats.effect.IO
import weaver.*
import cats.effect.std.Random

trait BaseSuite extends IOSuite:
  override type Res = Probe

  private lazy val cls = this.getClass.getSimpleName()

  /** Generates a string with given identifer, but scoped to both the suite
    * class name and a random alphanumeric identifier. This allows running
    * suites in parallel and avoid collisions in things such as identifiers
    *
    * @param n
    */
  def named(n: String): IO[String] = Random
    .scalaUtilRandom[IO]
    .flatMap(_.nextAlphaNumeric.replicateA(10).map(_.mkString))
    .map(cls + "-" + n + "-" + _)

  def probeTest(name: weaver.TestName)(f: Probe => IO[weaver.Expectations]) =
    test(name) { (probe, _) =>
      f(probe).attempt.flatMap(IO.fromEither)
    }
end BaseSuite
