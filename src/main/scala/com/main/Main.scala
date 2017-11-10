package com.main

import java.io.File

import com.model._
import play.api.libs.json.Json
import com.model.MyImplicits._

import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    if (args.length == 2) {
      val inputState = new File(args(0))
      val eventFile = new File(args(1))
      val initGraphState = Json.parse(Source.fromFile(file = inputState, enc = "UTF-8").mkString).as[GraphState]
      val events = Json.parse(Source.fromFile(file = eventFile, enc = "UTF-8").mkString).as[Events]
      process(initGraphState, events)
    } else {
      //this else is given so that if args is not given, still the code runs
      val initGraphState = Json.parse(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("sample-initial.json")).mkString).as[GraphState]
      val events = Json.parse(Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("sample-events.json")).mkString).as[Events]
      process(initGraphState, events)
    }

  }

  def process(initialGraphState: GraphState, events: Events): Unit = {
    val sortedEvents = Events(events.events.sortWith(_.timestamp < _.timestamp))
    val eventSeq = sortedEvents.events
    var graphState = initialGraphState
    for (e <- eventSeq) {
      graphState = processComponents(graphState, e)
    }
    val processedGraphJson = Json.toJson(graphState)
    println(Json.prettyPrint(processedGraphJson))
  }

  def processComponents(graphState: GraphState, event: Event): GraphState = {
    val eventComp = graphState.graph.components.find(a => a.id == event.component)
    val dependsOnEventComp = graphState.graph.components.filter(a => a.depends_on.exists(c => c.contains(event.component)))
    if (eventComp.isDefined) {
      val compAfterEventProcessing = applyCheckStateAndOwnStateAndSelfDerivedStateRule(eventComp.get, event)
      val updatedDependencyOf = applyDerivedStateRuleToDependency(compAfterEventProcessing, dependsOnEventComp)
      val componentsProcessed = updatedDependencyOf.+:(compAfterEventProcessing)
      val remainingComponents = graphState.graph.components.filterNot(a => componentsProcessed.exists(b => b.id == a.id))
      val allComponents = componentsProcessed.++:(remainingComponents)
      GraphState(graph = Graph(components = allComponents))
    } else {
      graphState
    }
  }

  def applyCheckStateAndOwnStateAndSelfDerivedStateRule(comp: Component, e: Event): Component = {
    val compWithCheck = e.check_state match {
      case CheckState.CPU_LOAD => comp.copy(check_states = comp.check_states.copy(`CPU load` = e.state))
      case CheckState.RAM_USAGE => comp.copy(check_states = comp.check_states.copy(`RAM usage` = e.state))
    }
    val maxState = Seq(compWithCheck.check_states.`CPU load`, compWithCheck.check_states.`RAM usage`).max
    val compWithOwn = compWithCheck.copy(own_state = maxState)
    if (maxState.compareTo(State.WARNING) >= 0 && maxState.compareTo(compWithOwn.derived_state) >= 0)
      compWithOwn.copy(derived_state = maxState)
    else
      compWithOwn
  }

  def applyDerivedStateRuleToDependency(comp: Component, dependencyOf: Seq[Component] = Nil): Seq[Component] = {
    dependencyOf.map(a => applyDerivedStateRuleToComp(a, comp))
  }

  def applyDerivedStateRuleToComp(comp: Component, dependOnComp: Component): Component = {
    if (dependOnComp.derived_state.compareTo(comp.derived_state) > 0) comp.copy(derived_state = dependOnComp.derived_state)
    else comp
  }


}
