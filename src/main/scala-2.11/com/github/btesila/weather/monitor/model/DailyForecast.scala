package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 *
 * @param dateTime
 * @param minimumTemperature
 * @param maximumTemperature
 * @param dayDescription
 * @param nightDescription
 */
case class DailyForecast(
    dateTime: DateTime,
    minimumTemperature: Temperature,
    maximumTemperature: Temperature,
    dayDescription: String,
    nightDescription: String)
