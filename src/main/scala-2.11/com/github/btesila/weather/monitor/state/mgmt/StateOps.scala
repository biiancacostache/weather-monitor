package com.github.btesila.weather.monitor.state.mgmt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.accuweather.AccuWeatherClient
import com.github.btesila.weather.monitor.model.ActiveLocationRecord
import org.joda.time.{DateTime, Months, Seconds}

trait StateOps {
  import State._

  /**
   *
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
   *
   */
  def cleanInactiveLocationRecords(ttl: Long): Unit = {
    def isPastTime(date: DateTime, ttl: Long): Boolean =
      DateTime.now.getMillis - date.getMillis >= ttl

    activeLocationRecords.filter { case (_, ActiveLocationRecord(time, _, _)) => isPastTime(time, ttl) }
      .foreach { case entry => removeLocationRecord(entry)}
  }
}
