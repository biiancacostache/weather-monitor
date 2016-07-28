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

trait WeatherMonitorOps extends LazyLogging {

  import State._

  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer

  import system.dispatcher

  protected lazy val awc = AccuWeatherClient()

  /**
   *
   * @param city
   * @param country
   * @return
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
   *
   * @param city
   * @param country
   * @return
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
   *
   * @param city
   * @param country
   * @return
   */
  def getExtendedForecast(city: String, country: String): Future[(ActiveLocationRecord, ExtendedForecast)] = {
    for {
      locationRecord <- getLocationCrtRecord(city, country)
      forecast       <- awc.getExtendedForecast(locationRecord.location.key)
    } yield (locationRecord, forecast)
  }

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
