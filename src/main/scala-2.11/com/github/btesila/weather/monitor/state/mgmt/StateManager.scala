package com.github.btesila.weather.monitor.state.mgmt

import akka.actor.{Actor, Props}
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.state.mgmt.StateManager.{Clean, Poll}
import com.typesafe.scalalogging.LazyLogging

/**
 * Actor responsible for retrieving temperature samples for the locations stored in the service state, as well as
 * cleaning up the data records for inactive locations (that have not been triggered for a configurable amount of time).
 */
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

  /**
   * This is an actor message used for signaling the cleanup process.
   *
   * @param ttl the time frame taken into consideration when marking a location as inactive.
   */
  case class Clean(ttl: Long)

  /**
   * This is an actor message used for signaling the temperature sampling process.
   */
  case object Poll
}