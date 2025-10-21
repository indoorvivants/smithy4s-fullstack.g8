package hellosmithy4s

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import scala.concurrent.ExecutionContext
import hellosmithy4s.Page.NameSummary

def summaryPage(signal: Signal[NameSummary])(using
    router: Router[Page],
    api: Api,
    ec: ExecutionContext
) =
  div(
    h1(
      child.text <-- signal.map(_.id.value)
    )
  )
