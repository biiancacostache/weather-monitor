package com.github.btesila.weather.monitor.accuweather

import akka.actor.{ActorLogging, ActorSystem}
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.Fault.{LocationNotSupported, MissingWeatherInformation}
import com.github.btesila.weather.monitor.Settings
import com.github.btesila.weather.monitor.model._

import scala.concurrent.Future

trait AccuWeatherClient extends WeatherMonitorProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  import system.dispatcher

  private lazy val settings = Settings(system).Accuweather

  def getLocation(city: String, country: String): Future[Option[Location]] = {
    val locationUri = Uri(settings.LocationUri)
    val uri = locationUri.withQuery(Query("q" -> s"$city,$country"))
    for {
      resp   <- send(Get(uri))
      resp   <- expect(resp, StatusCodes.OK, LocationNotSupported)
      result <- unmarshal[List[Location]](resp)
    } yield result.headOption
  }

  def getCurrentConditions(locationKey: String): Future[Option[CurrentWeatherConditions]] = {
    val crtConditionsUri = Uri(settings.CurrentConditionUri)
    val uri = crtConditionsUri.copy(path = crtConditionsUri.path / locationKey)
    for {
      resp    <- send(Get(uri))
      resp    <- expect(resp, StatusCodes.OK, MissingWeatherInformation)
      result  <- unmarshal[List[CurrentWeatherConditions]](resp)
    } yield result.headOption
  }

  def getDailyForecast(locationKey: String): Future[Option[DailyForecast]] = {
    val dailyForecastUri = Uri(settings.DailyForecastUri)
    val uri = dailyForecastUri.copy(path = dailyForecastUri.path / locationKey).withQuery(Query("metric" -> "true"))
    for {
      resp   <- send(Get(uri))
      resp   <- expect(resp, StatusCodes.OK, MissingWeatherInformation)
      result <- unmarshal[ExtendedForecast](resp)
    } yield result.dailyForecasts.headOption
  }

  def getExtendedForecast(locationKey: String): Future[ExtendedForecast] = {
    val extendedForecastUri = Uri(settings.ExtendedForecastUri)
    val uri = extendedForecastUri.copy(path = extendedForecastUri.path / locationKey).withQuery(Query("metric" -> "true"))
    for {
      resp             <- send(Get(uri))
      resp             <- expect(resp, StatusCodes.OK, MissingWeatherInformation)
      extendedForecast <- unmarshal[ExtendedForecast](resp)
    } yield extendedForecast
  }
}

object AccuWeatherClient {
  def apply()(implicit as: ActorSystem, mat: ActorMaterializer) = new AccuWeatherClient {
    override implicit val materializer: ActorMaterializer = mat
    override implicit val system: ActorSystem = as
  }
}