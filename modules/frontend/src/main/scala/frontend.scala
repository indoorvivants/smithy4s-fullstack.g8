package hellosmithy4s

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.Router
import com.raquo.waypoint.SplitRender
import org.scalajs.dom

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
