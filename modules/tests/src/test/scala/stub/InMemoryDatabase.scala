package hellosmithy4s
package stub

import cats.effect.*
import hellosmithy4s.spec.*

class InMemoryDatabase private (ref: Ref[IO, Map[Name, Item]]) extends Database:
  override def add(name: Name): IO[Unit] =
    ref
      .modify: m =>
        if m.contains(name) then
          m -> IO.raiseError(Exception(s"Name $name already exists"))
        else m.updated(name, Item(name = name)) -> IO.unit
      .flatten
  override def list(): IO[List[Item]] =
    ref.get.map(_.values.toList.sortBy(_.name.value))
end InMemoryDatabase

object InMemoryDatabase:
  def create = IO.ref(Map.empty).map(InMemoryDatabase(_))
