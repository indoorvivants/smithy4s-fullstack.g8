package hellosmithy4s

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import scala.concurrent.ExecutionContext
import scala.util.*
import hellosmithy4s.spec.NameAlreadyExists

def indexPage(using
    Router[Page]
)(using api: Api, events: EventBus[Event], ec: ExecutionContext) =
  inline def notification =
    events.emit(Event.KeyUpdated(scala.util.Random().nextInt()))

  val error = Var("")

  val addForm = AddKeyForm(
    Observer: pair =>
      error.set("")
      api
        .future(_.hello.add(pair.name))
        .onComplete:
          case Failure(exc: NameAlreadyExists) => 
            error.set("This name is already taken!")

          case Failure(exc) => 
            org.scalajs.dom.console.log(exc)
            error.set("Server error :(")
          case Success(_)   => 
            error.set("")
            notification
  )

  div(
    child <-- error.signal.map:
      case ""  => emptyNode
      case msg => div(cls := "errormsg", msg),
    addForm.node,
    div(
      cls := "rows",
      children <--
        events.toObservable.startWithNone.flatMapSwitch { _ =>
          api
            .stream(_.hello.list())
            .map { items =>
              items.items.map { item =>
                ul(
                  li(b(item.name.value))
                )
              }
            }
        }
    )
  )
end indexPage
