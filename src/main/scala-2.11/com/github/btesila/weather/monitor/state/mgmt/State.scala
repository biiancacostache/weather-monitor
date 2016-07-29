package com.github.btesila.weather.monitor.state.mgmt

import java.util.concurrent.ConcurrentHashMap

import com.github.btesila.weather.monitor.model.{ActiveLocationRecord, CurrentWeatherConditions, Location}
import org.joda.time._

import scala.collection._
import scala.collection.convert.decorateAsScala._

/**
 * Holds the service state by storing weather information for frequently triggered locations.
 */
object State {

  val activeLocationRecords: concurrent.Map[String, ActiveLocationRecord] =
    new ConcurrentHashMap[String, ActiveLocationRecord]().asScala

  def recordIdFor(location: Location): String = recordIdFor(location.city.toLowerCase, location.country.toLowerCase)

  def recordIdFor(city: String, country: String): String = s"${city.toLowerCase}-${country.toLowerCase}"

  def findActiveLocation(city: String, country: String): Option[ActiveLocationRecord] = {
    val id = recordIdFor(city, country)
    activeLocationRecords.get(id) match {
      case Some(record) => {
        activeLocationRecords.put(id, record.copy(lastTriggered = DateTime.now))
        Some(record)
      }
      case _ => None
    }
  }

  def addLocationRecord(location: Location, crtWeatherConditions: CurrentWeatherConditions): Unit = {
    val id = recordIdFor(location)
    activeLocationRecords.put(id, ActiveLocationRecord(DateTime.now, location, crtWeatherConditions))
  }

  def updateLocationRecord(id: String, crtConditions: CurrentWeatherConditions): Unit =
    activeLocationRecords.get(id).foreach { record =>
      activeLocationRecords.put(id, record.copy(crtConditions = crtConditions))
    }

  def removeLocationRecord(entry: (String, ActiveLocationRecord)): Unit = {
    val (id, activeLocation) = entry
    activeLocationRecords.remove(id, activeLocation)
  }
}
