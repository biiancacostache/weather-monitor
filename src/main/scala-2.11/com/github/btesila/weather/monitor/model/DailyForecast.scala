package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 * Data type representing a daily weather forecast.
 *
 * @param dateTime the observation time
 * @param minimumTemperature the minimum temperature information
 * @param maximumTemperature the maximum temperature information
 * @param dayDescription the description of the weather conditions during the day
 * @param nightDescription the description of the weather conditions during the night
 */
case class DailyForecast(
    dateTime: DateTime,
    minimumTemperature: Temperature,
    maximumTemperature: Temperature,
    dayDescription: String,
    nightDescription: String)
