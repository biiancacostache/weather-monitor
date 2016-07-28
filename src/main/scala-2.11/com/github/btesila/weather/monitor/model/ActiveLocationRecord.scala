package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime

/**
 *
 * @param lastTriggered
 * @param location
 * @param crtConditions
 */
case class ActiveLocationRecord(lastTriggered: DateTime, location: Location, crtConditions: CurrentWeatherConditions)
