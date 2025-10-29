package hellosmithy4s

import hellosmithy4s.spec.Name

trait HelloSuite:
  self: BaseSuite =>

  probeTest("Adding") { probe =>
    for
      key <- named("hello").map(Name(_))
      _   <- probe.api.hello.add(key)
      lst <- probe.api.hello.list()
    yield expect(lst.items.exists(_.name == key))
  }

  probeTest("Adding same name is not allowed") { probe =>
    for
      key  <- named("hello").map(Name(_))
      res1 <- probe.api.hello.add(key).attempt
      res2 <- probe.api.hello.add(key).attempt
    yield expect.all(
      clue(res1).isRight,
      clue(res2).isLeft,
    )
  }
end HelloSuite
