package com.github.btesila.weather.monitor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

import scala.concurrent.duration.FiniteDuration

/**
 * Akka extension that exposes the configuration settings of the application actor system.
 *
 * @param system the source actor system used to extract the configuration settings
 */
class Settings(system: ActorSystem) extends Extension {
  object WeatherMonitor {
    private val ns = system.settings.config.getConfig("weather-monitor")

    object Acceptor {
      private val http = ns.getConfig("http")
      /**
       * The endpoint the service listens for Http requests on
       */
      val Host = http.getString("host")
      /**
       * The port the service listens for Http requests on
       */
      val Port = http.getInt("port")
    }

    object State {
      private val state = ns.getConfig("state")

      object Polling {
        private val polling = state.getConfig("polling")
        /**
         * The initial delay before starting to sample temperature records for active locations.
         */
        val InitialDelay = FiniteDuration(
          polling.getDuration("initial-delay", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
        /**
         * The time interval for sampling temperature records
         */
        val Interval = FiniteDuration(
          polling.getDuration("interval", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
      }

      object Cleanup {
        private val cleanup = state.getConfig("cleanup")
        /**
         * The initial delay before starting to clean.
         */
        val InitialDelay = FiniteDuration(
          cleanup.getDuration("initial-delay", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
        /**
         * The initial delay before starting to cleanup temperature records belonging to inactive locations.
         */
        val Interval = FiniteDuration(
          cleanup.getDuration("interval", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
      }
    }
  }

  object Accuweather {
    private val ns = system.settings.config.getConfig("accuweather")
    /**
     * The API key used for sending requests to Accuweather
     */
    val ApiKey = ns.getString("api-key")
    /**
     * The URI for retrieving information with regards to a given location.
     */
    val LocationUri = ns.getString("location-uri")
    /**
     * The URI for retrieving current weather conditions with regards to a given location.
     */
    val CurrentConditionUri = ns.getString("current-condition-uri")
    /**
     * The URI for retrieving the daily weather forecast with regards to a given location.
     */
    val DailyForecastUri = ns.getString("daily-forecast-uri")
    /**
     * The URI for retrieving the weekly weather forecast with regards to a given location.
     */
    val ExtendedForecastUri = ns.getString("extended-forecast-uri")
  }
}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Settings = new Settings(system)
  override def lookup(): ExtensionId[_ <: Extension] = Settings
}
