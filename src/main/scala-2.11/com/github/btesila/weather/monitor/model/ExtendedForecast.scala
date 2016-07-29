package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 * Data type representing an extended (weekly) weather forecast.
 *
 * @param startDateTime the start time of the forecast
 * @param endDateTime the end time of the forecast
 * @param description the description of the forecast
 * @param dailyForecasts a list of daily forecasts with regards to the forecast time frame
 */
case class ExtendedForecast(
    startDateTime: DateTime,
    endDateTime: DateTime,
    description: String,
    dailyForecasts: List[DailyForecast])
