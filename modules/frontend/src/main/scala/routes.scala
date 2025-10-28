package hellosmithy4s

import scala.scalajs.js.JSON

import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import hellosmithy4s.spec.*
import io.circe.*
import io.circe.syntax.*
import smithy4s.Newtype

def codec[A: Decoder: Encoder](nt: Newtype[A]): Codec[nt.Type] =
  val decT = summon[Decoder[A]].map(nt.apply)
  val encT = summon[Encoder[A]].contramap(nt.value)

  Codec.from(decT, encT)

given Codec[Name] = codec(Name)

sealed trait Page derives Codec.AsObject
object Page:
  case object Index                extends Page
  case class NameSummary(id: Name) extends Page

  val mainRoute = Route.static(Page.Index, root / endOfSegments)

  val helloRoute = Route(
    encode = (stp: NameSummary) => stp.id.value,
    decode = (arg: String) => NameSummary(Name(arg)),
    pattern = root / "summary" / segment[String] / endOfSegments
  )

  val router = new Router[Page](
    routes = List(
      mainRoute,
      helloRoute
    ),
    getPageTitle = {
      case Index           => "Hello from Smithy4s!"
      case NameSummary(id) => s"Summary for key ${id.value}"
    },
    serializePage = pg => pg.asJson.noSpaces,
    deserializePage = str =>
      io.circe.scalajs.decodeJs[Page](JSON.parse(str)).fold(throw _, identity)
  )
end Page

def redirectTo(pg: Page)(using router: Router[Page]) =
  router.pushState(pg)

def forceRedirectTo(pg: Page)(using router: Router[Page]) =
  router.replaceState(pg)

def navigateTo(page: Page)(using router: Router[Page]): Binder[HtmlElement] =
  Binder { el =>
    import org.scalajs.dom

    val isLinkElement = el.ref.isInstanceOf[dom.html.Anchor]

    if isLinkElement then el.amend(href(router.absoluteUrlForPage(page)))

    (onClick
      .filter(ev =>
        !(isLinkElement && (ev.ctrlKey || ev.metaKey || ev.shiftKey || ev.altKey))
      )
      .preventDefault
      --> (_ => redirectTo(page))).bind(el)
  }
