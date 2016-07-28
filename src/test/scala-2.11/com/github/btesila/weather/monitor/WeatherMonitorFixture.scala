package com.github.btesila.weather.monitor

import com.github.btesila.weather.monitor.model._
import org.joda.time.DateTime

object WeatherMonitorFixture {
  val city = "Bucharest"
  val country = "Romania"
  val locationKey = "28430"
  val location = Location(locationKey, city, country)
  val temperature = Temperature(32, "C")
  val description = "description"
  val crtConditions = CurrentWeatherConditions(DateTime.now, "Sunny", true, temperature)
  val activeLocationRecord = ActiveLocationRecord(DateTime.now, location, crtConditions)
  val dailyForecast = DailyForecast(DateTime.now, temperature, temperature, description, description)
  val extendedForecast = ExtendedForecast(DateTime.now, DateTime.now, description, List(dailyForecast))
}
