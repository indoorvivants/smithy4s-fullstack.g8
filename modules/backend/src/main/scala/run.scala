package hellosmithy4s

import cats.effect.IOApp

object Run extends IOApp:
  def run(args: List[String]) =
    bootstrap(args, sys.env).useForever
