package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 *
 * @param startDateTime
 * @param endDateTime
 * @param description
 * @param dailyForecasts
 */
case class ExtendedForecast(
    startDateTime: DateTime,
    endDateTime: DateTime,
    description: String,
    dailyForecasts: List[DailyForecast])
