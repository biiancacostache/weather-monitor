package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 * Data type representing an active location data record.
 *
 * @param lastTriggered the date corresponding to the last request triggered for this location
 * @param location the location details
 * @param crtConditions the current weather conditions
 */
case class ActiveLocationRecord(lastTriggered: DateTime, location: Location, crtConditions: CurrentWeatherConditions)
