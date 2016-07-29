package com.github.btesila.weather.monitor.model

/**
 * Data type representing a location.
 *
 * @param key a unique identifier for the location
 * @param city the city name
 * @param country the country name
 */
case class Location(key: String, city: String, country: String)
