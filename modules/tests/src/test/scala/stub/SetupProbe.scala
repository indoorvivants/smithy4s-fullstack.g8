package hellosmithy4s
package stub

private trait SetupProbe:
  self: BaseSuite =>
  override def sharedResource = buildApp
