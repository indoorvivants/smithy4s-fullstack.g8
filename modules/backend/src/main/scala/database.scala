package hellosmithy4s

import cats.effect.IO

import spec.*

trait Database:
  def list(): IO[List[Item]]
  def add(name: Name): IO[Unit]
