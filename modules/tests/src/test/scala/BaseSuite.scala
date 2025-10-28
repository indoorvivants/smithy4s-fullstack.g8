package hellosmithy4s

import cats.effect.IO
import weaver.*

trait BaseSuite extends IOSuite:
  override type Res = Probe

  def probeTest(name: weaver.TestName)(f: Probe => IO[weaver.Expectations]) =
    test(name) { (probe, _) =>
      f(probe).attempt.flatMap(IO.fromEither)
    }
end BaseSuite
