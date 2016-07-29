package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 * Data type representing the current weather conditions with regards to a location.
 *
 * @param dateTime the observation time
 * @param description the description of the current weather conditions
 * @param isDayTime a flag signaling whether it is day time or not
 * @param temperature the current temperature information.
 */
case class CurrentWeatherConditions(dateTime: DateTime, description: String, isDayTime: Boolean, temperature: Temperature)

