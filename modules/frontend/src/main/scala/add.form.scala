package hellosmithy4s

import com.raquo.laminar.api.L.*
import hellosmithy4s.spec.*

class AddKeyForm private (val node: Element)
object AddKeyForm:
  def apply(obs: Observer[Item]) =

    val nameVar = Var(Option.empty[Name])

    val node =
      form(
        onSubmit.preventDefault --> { _ =>
          nameVar.now().foreach { name =>
            obs.onNext(Item(name))
            nameVar.set(None)
          }
        },
        div(
          cls := "add-form",
          div(
            cls := "add-form-row",
            p(cls := "lead", "Name:"),
            input(
              idAttr      := "input-key",
              tpe         := "text",
              placeholder := "e.g. Tony",
              cls         := "inp",
              value <-- nameVar.signal.map(_.map(_.value).getOrElse("")),
              onInput.mapToValue
                .map(_.trim)
                .map(s =>
                  Option.when(s.nonEmpty)(s).map(Name(_))
                ) --> nameVar.writer
            )
          ),
          div(
            cls := "add-form-row",
            button(
              tpe    := "submit",
              idAttr := "input-submit",
              "add",
              cls := "btn"
            )
          )
        )
      )

    new AddKeyForm(node)
  end apply
end AddKeyForm
