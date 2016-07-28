package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 *
 * @param dateTime
 * @param description
 * @param isDayTime
 * @param temperature
 */
case class CurrentWeatherConditions(dateTime: DateTime, description: String, isDayTime: Boolean, temperature: Temperature)

