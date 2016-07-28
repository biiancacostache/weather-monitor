package com.github.btesila.weather.monitor

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}

import scala.concurrent.duration.FiniteDuration

class Settings(system: ActorSystem) extends Extension {
  object WeatherMonitor {
    private val ns = system.settings.config.getConfig("weather-monitor")

    object Acceptor {
      private val http = ns.getConfig("http")
      /**
       *
       */
      val Host = http.getString("host")
      /**
       *
       */
      val Port = http.getInt("port")
    }

    object State {
      private val state = ns.getConfig("state")

      object Polling {
        private val polling = state.getConfig("polling")
        /**
         *
         */
        val InitialDelay = FiniteDuration(
          polling.getDuration("initial-delay", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
        /**
         *
         */
        val Interval = FiniteDuration(
          polling.getDuration("interval", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
      }

      object Cleanup {
        private val cleanup = state.getConfig("cleanup")
        /**
         *
         */
        val InitialDelay = FiniteDuration(
          cleanup.getDuration("initial-delay", TimeUnit.MILLISECONDS),
          TimeUnit.MILLISECONDS)
        /**
         *
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
     *
     */
    val ApiKey = ns.getString("api-key")
    /**
     *
     */
    val LocationUri = ns.getString("location-uri")
    /**
     *
     */
    val CurrentConditionUri = ns.getString("current-condition-uri")
    /**
     *
     */
    val DailyForecastUri = ns.getString("daily-forecast-uri")
    /**
     *
     */
    val ExtendedForecastUri = ns.getString("extended-forecast-uri")
  }
}

object Settings extends ExtensionId[Settings] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Settings = new Settings(system)
  override def lookup(): ExtensionId[_ <: Extension] = Settings
}
