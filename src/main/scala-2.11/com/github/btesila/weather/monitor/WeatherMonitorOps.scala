package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.Fault.{LocationNotSupported, MissingWeatherInformation}
import com.github.btesila.weather.monitor.accuweather.AccuWeatherClient
import com.github.btesila.weather.monitor.model._
import com.github.btesila.weather.monitor.state.mgmt.State
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Provides all the Weather Monitor operations.
 */
trait WeatherMonitorOps extends LazyLogging {

  import State._

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  import system.dispatcher

  protected lazy val awc = AccuWeatherClient()

  /**
   * Retrieve the current weather conditions, given a location through the medium of the city name
   * and country name. If the location is marked as active, the weather information is retrieved
   * from the service state. Otherwise, a new request is sent to the weather information provider.
   *
   * @param city the city name
   * @param country the country name
   * @return an ActiveLocationRecord object
   */
  def getLocationCrtRecord(city: String, country: String): Future[ActiveLocationRecord] =
    findActiveLocation(city, country) match {
      case Some(activeLocation) =>
        logger.info("Retrieving location record from the service cache.")
        Future.successful(activeLocation)
      case _ =>
        logger.info("Retrieving location record from the server.")
        fetchLocationCrtRecord(city, country)
    }

  /**
   * Retrieve the daily weather forecast along the current weather conditions, given a location
   * through the medium of the city name and country name.
   *
   * @param city the city name
   * @param country the country name
   * @return a tuple containing an ActiveLocationRecord object along a DailyForecast object
   */
  def getDailyForecast(city: String, country: String): Future[(ActiveLocationRecord, DailyForecast)] =
    getLocationCrtRecord(city, country) flatMap { locationRecord =>
      awc.getDailyForecast(locationRecord.location.key) map {
        case Some(dailyCond) => (locationRecord, dailyCond)
        case _               =>
          logger.debug("Could not retrieve the daily forecast for {} {}", city, country)
          throw MissingWeatherInformation
      }
  }

  /**
   * Retrieve the extended(weekly) weather forecast along the current weather conditions, given a location
   * through the medium of the city name and country name.
   *
   * @param city the city name
   * @param country the country name
   * @return a tuple containing an ActiveLocationRecord object along an ExtendedForecast object
   */
  def getExtendedForecast(city: String, country: String): Future[(ActiveLocationRecord, ExtendedForecast)] = {
    for {
      locationRecord <- getLocationCrtRecord(city, country)
      forecast       <- awc.getExtendedForecast(locationRecord.location.key)
    } yield (locationRecord, forecast)
  }

  /**
   * Retrieve the current weather conditions from the weather information provider, given a location through the medium
   * of the city name and country name.
   *
   * @param city the city name
   * @param country the country name
   * @return an ActiveLocationRecord object
   */
  protected def fetchLocationCrtRecord(city: String, country: String): Future[ActiveLocationRecord] = {
    awc.getLocation(city, country) flatMap {
      case Some(location) =>
        awc.getCurrentConditions(location.key) map {
          case Some(crtConditions) => {
            addLocationRecord(location, crtConditions)
            ActiveLocationRecord(DateTime.now, location, crtConditions)
          }
          case _ =>
            logger.debug("Failed to retrieve the current weather conditions for {} {}", city, country)
            throw MissingWeatherInformation
      }
      case _ =>
        logger.debug("Failed to retrieve location information for {} {}", city, country)
        Future.failed(LocationNotSupported)
    }
  }
}

object WeatherMonitorOps {
  def apply()(implicit
    as: ActorSystem,
    materializer: ActorMaterializer): WeatherMonitorOps =
    new WeatherMonitorOps {
      override implicit val system  = as
      override implicit val mat = materializer
    }
}
