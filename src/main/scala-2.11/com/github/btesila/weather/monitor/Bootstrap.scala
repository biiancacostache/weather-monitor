package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.state.mgmt.StateManager
import com.github.btesila.weather.monitor.state.mgmt.StateManager.{Clean, Poll}
import com.typesafe.config.ConfigFactory

object Bootstrap extends App {

  val config                = ConfigFactory.defaultApplication().resolve()
  implicit val system       = ActorSystem("weather-monitor", config)
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val stateSettings = Settings(system).WeatherMonitor.State
  val stateManager = system.actorOf(StateManager.props(materializer))

  // Start the server
  RestService().start()

  // Sample temperatures for active locations
  system.scheduler.schedule(
    initialDelay = stateSettings.Polling.InitialDelay,
    interval = stateSettings.Polling.Interval,
    stateManager,
    Poll)

  // Remove data records for inactive locations
  val cleanupInterval = stateSettings.Cleanup.Interval
  system.scheduler.schedule(
    initialDelay = stateSettings.Cleanup.InitialDelay,
    interval = cleanupInterval,
    stateManager,
    Clean(cleanupInterval.length))
}
