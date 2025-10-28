package hellosmithy4s
package stub

import cats.effect.IO
import cats.effect.kernel.Resource

private trait SetupProbe:
  self: BaseSuite =>
  override def sharedResource = buildApp
