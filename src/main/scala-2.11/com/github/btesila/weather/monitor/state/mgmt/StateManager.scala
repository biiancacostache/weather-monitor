package com.github.btesila.weather.monitor.state.mgmt

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.state.mgmt.StateManager.{Clean, Poll}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration

class StateManager(mat: ActorMaterializer) extends Actor with StateOps with LazyLogging {
  override def receive: Receive = {
    case Poll  =>
      logger.info("Polling temperature samples for active locations...")
      pollTemperatureSamples()(context.system, mat)
    case Clean(ttl) =>
      logger.info("Cleaning data records for inactive locations...")
      cleanInactiveLocationRecords(ttl)
  }
}

object StateManager {
  def props(mat: ActorMaterializer): Props = Props(new StateManager(mat))

  case class Clean(ttl: Long)
  case object Poll
}