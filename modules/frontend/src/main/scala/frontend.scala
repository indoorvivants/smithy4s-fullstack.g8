package hellosmithy4s

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.Router
import org.scalajs.dom
import com.raquo.waypoint.SplitRender

import spec.*

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

enum Event:
  case KeyUpdated(i: Int)

@main def frontend =
  given Router[Page]    = Page.router
  given Api             = Api.create()
  given EventBus[Event] = new EventBus[Event]

  val app = div(
    child <-- renderPage
  )

  renderOnDomContentLoaded(
    dom.document.getElementById("root"),
    app
  )

end frontend

def renderPage(using
    router: Router[Page]
)(using Api, EventBus[Event]): Signal[HtmlElement] =
  SplitRender[Page, HtmlElement](router.currentPageSignal)
    .collectStatic(Page.Index)(indexPage)
    .collectSignal[Page.NameSummary](summaryPage)
    .signal
