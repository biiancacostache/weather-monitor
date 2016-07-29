package com.github.btesila.weather.monitor.state.mgmt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.accuweather.AccuWeatherClient
import com.github.btesila.weather.monitor.model.ActiveLocationRecord
import org.joda.time.DateTime

/**
 * Provides all the operation for the service state management.
 */
trait StateOps {
  import State._

  /**
   * Retrieve temperature samples for active locations - stored in the service state due to the frequent requests aiming
   * information with regards to them.
   *
   * @param system required implicit actor system
   * @param materializer required implicit actor materializer
   */
  def pollTemperatureSamples()(implicit system: ActorSystem, materializer: ActorMaterializer): Unit = {
    import system.dispatcher
    val awc = AccuWeatherClient()
    activeLocationRecords foreach { case (id, ActiveLocationRecord(_, location, _)) =>
      awc.getCurrentConditions(location.key) map { crtCond =>
        crtCond.foreach(updateLocationRecord(id, _))
      }
    }
  }

  /**
   * Clean data records for inactive locations - stored in the service state, but not triggered for a given amount of time.
   *
   * @param ttl the time frame after which a location is considered to be inactive.
   */
  def cleanInactiveLocationRecords(ttl: Long): Unit = {
    def isPastTime(date: DateTime, ttl: Long): Boolean =
      DateTime.now.getMillis - date.getMillis >= ttl

    activeLocationRecords.filter { case (_, ActiveLocationRecord(time, _, _)) => isPastTime(time, ttl) }
      .foreach { case entry => removeLocationRecord(entry)}
  }
}
