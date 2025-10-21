package hellosmithy4s

import cats.effect.IO

import spec.*

import cats.*, cats.data.*, cats.implicits.*
import skunk.{Codec, Query}
import skunk.implicits.*
import skunk.codec.all.*

import smithy4s.Newtype
import skunk.data.Completion
import skunk.Command

trait Database:
  def list(): IO[List[Item]]
  def add(name: Name): IO[Unit]
