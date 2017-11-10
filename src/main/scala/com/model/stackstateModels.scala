package com.model

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import MyImplicits._
import scala.collection.Seq
import scala.util.Try // Combinator syntax

//Read/Write for Java Based Enum
object MyImplicits {
  implicit val stateReads: Reads[State] = new Reads[State] {
    override def reads(json: JsValue): JsResult[State] = {
      json match {
        case JsString(str) =>
          if (Try(State.toEnum(str)).isSuccess) JsSuccess(State.toEnum(str))
          else JsError(Seq(JsPath -> Seq(JsonValidationError("Expected Valid Enum Value"))))
        case _ => JsError(Seq(JsPath -> Seq(JsonValidationError("Expected Enum String"))))
      }
    }
  }
  implicit val stateWrites: Writes[State] = new Writes[State] {
    override def writes(o: State): JsValue = {
      JsString(o.getValue)
    }
  }
  implicit val stateFormats: Format[State] = Format(stateReads, stateWrites)

  implicit val checkStateReads: Reads[CheckState] = new Reads[CheckState] {
    override def reads(json: JsValue): JsResult[CheckState] = {
      json match {
        case JsString(str) =>
          if (Try(CheckState.toEnum(str)).isSuccess) JsSuccess(CheckState.toEnum(str))
          else JsError(Seq(JsPath -> Seq(JsonValidationError("Expected Valid Enum Value"))))
        case _ => JsError(Seq(JsPath -> Seq(JsonValidationError("Expected Enum String"))))
      }
    }
  }
  implicit val checkStateWrites: Writes[CheckState] = new Writes[CheckState] {
    override def writes(o: CheckState): JsValue = {
      JsString(o.getValue)
    }
  }
  implicit val checkStateFormats: Format[CheckState] = Format(checkStateReads, checkStateWrites)
}

// Stack State Classes

case class CheckStates(`CPU load`: State = State.NO_DATA, `RAM usage`: State = State.NO_DATA)

object CheckStates {
  implicit val formats = Json.format[CheckStates]
}

case class Component(id: String,
                     own_state: State,
                     derived_state: State,
                     check_states: CheckStates,
                     depends_on: Option[Seq[String]] = None,
                     dependency_of: Option[Seq[String]] = None)

object Component {
  implicit val formats = Json.format[Component]
}

case class Graph(components: Seq[Component] = Nil)

object Graph {
  implicit val formats = Json.format[Graph]
}

case class GraphState(graph: Graph)

object GraphState {
  implicit val formats = Json.format[GraphState]
}

// Event related classes

case class Event(timestamp: String, component: String, check_state: CheckState, state: State)

object Event {
  implicit val formats = Json.format[Event]
}

case class Events(events: Seq[Event] = Nil)

object Events {
  implicit val formats = Json.format[Events]
}

