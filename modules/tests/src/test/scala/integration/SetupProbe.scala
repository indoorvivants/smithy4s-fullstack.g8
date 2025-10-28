package hellosmithy4s
package integrationtest

private trait SetupProbe:
  self: BaseSuite =>
  override def sharedResource = buildApp().map(_._1)
