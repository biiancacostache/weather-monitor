package com.github.btesila.weather.monitor.state.mgmt

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.accuweather.AccuWeatherClient
import com.github.btesila.weather.monitor.model.ActiveLocationRecord
import org.joda.time.{DateTime, Seconds}

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
  def cleanInactiveLocationRecords(): Unit = {
    def isPastOneMonth(date: DateTime): Boolean = {
      //      Months.monthsBetween(date, DateTime.now).getMonths >= 1
      //      Minutes.minutesBetween(date, DateTime.now).getMinutes > 1

      Seconds.secondsBetween(date, DateTime.now).getSeconds > 15
    }
    activeLocationRecords.filter { case (_, ActiveLocationRecord(time, _, _)) => isPastOneMonth(time) }
      .foreach { case entry => removeLocationRecord(entry)}
  }
}
