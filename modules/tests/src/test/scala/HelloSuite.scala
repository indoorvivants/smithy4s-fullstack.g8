package hellosmithy4s

import hellosmithy4s.spec.Name

trait HelloSuite:
  self: BaseSuite =>

  probeTest("Adding") { probe =>
    val key = Name("creating")

    for
      _   <- probe.api.hello.add(key)
      lst <- probe.api.hello.list()
    yield expect(lst.items.exists(_.name == key))
  }
end HelloSuite
