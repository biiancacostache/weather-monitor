package com.github.btesila.weather.monitor.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.github.btesila.weather.monitor.WeatherMonitorOps
import com.github.btesila.weather.monitor.model.WeatherMonitorProtocol
import spray.json.RootJsonWriter

import scala.concurrent.Future

/**
 * Defines the routes corresponding to the exposed API of the Weather Monitor.
 */
trait WeatherMonitorRoutes extends WeatherMonitorProtocol {
  def ops: WeatherMonitorOps

  val routes = pathPrefix("weather") {
    (get & path("current" / "conditions")) {
        parameters('city.as[String], 'country.as[String]) { (city, country) =>
          completeWith(StatusCodes.OK, ops.getLocationCrtRecord(city, country))
        }
      } ~
    (get & path("today")) {
      parameters('city.as[String], 'country.as[String]) { (city, country) =>
        completeWith(StatusCodes.OK, ops.getDailyForecast(city, country))
      }
    } ~
    (get & path("forecast")) {
      parameters('city.as[String], 'country.as[String]) { (city, country) =>
        completeWith(StatusCodes.OK, ops.getExtendedForecast(city, country))
      }
    }
  }

  private def completeWith[A: RootJsonWriter](statusCode: StatusCode, op: Future[A]): Route =
    extractExecutionContext { implicit ec =>
      complete {
        op map { (statusCode, _) }
      }
    }
}

object WeatherMonitorRoutes {
  def apply(wmo: WeatherMonitorOps): WeatherMonitorRoutes = new WeatherMonitorRoutes {
    override def ops: WeatherMonitorOps = wmo
  }
}
