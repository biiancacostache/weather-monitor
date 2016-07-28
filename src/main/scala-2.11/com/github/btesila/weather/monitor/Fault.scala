package com.github.btesila.weather.monitor

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.btesila.weather.monitor.model.Error
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.control.NoStackTrace

sealed abstract class Fault extends Exception with NoStackTrace

object Fault {

  case object InternalServiceError extends Fault {
    val error = Error(
      InternalServerError.intValue.toString,
      "internal_server_error",
      Some("Internal server error")
    )
  }

  /**
   *
   */
  case object LocationNotSupported extends Fault {
    val error = Error(
      NotFound.intValue.toString,
      "not_found",
      Some("This location is not supported.")
    )
  }

  case object MissingWeatherInformation extends Fault {
    val error = Error(
      NotFound.intValue.toString,
      "not_found",
      Some("Could not retrieve weather information for this location.")
    )
  }

  /**
   *
   */
  val Handler: PartialFunction[Throwable, Route] = {
    case LocationNotSupported      => complete(NotFound -> LocationNotSupported.error)
    case MissingWeatherInformation => complete(NotFound -> MissingWeatherInformation.error)
    case _                         => complete(InternalServerError -> InternalServiceError.error)
  }
}