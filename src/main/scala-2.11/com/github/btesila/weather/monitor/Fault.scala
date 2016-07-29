package com.github.btesila.weather.monitor

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.btesila.weather.monitor.model.Error
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.control.NoStackTrace

/**
 * Base type for all the faults triggered by the incoming requests.
 */
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
   * This fault occurs when the service cannot provide weather information with regards to a given location.
   */
  case object LocationNotSupported extends Fault {
    val error = Error(
      NotFound.intValue.toString,
      "not_found",
      Some("This location is not supported.")
    )
  }

  /**
   * This fault occurs when the service receives empty content when fetching data from the weather information provider.
   */
  case object MissingWeatherInformation extends Fault {
    val error = Error(
      NotFound.intValue.toString,
      "not_found",
      Some("Could not retrieve weather information for this location.")
    )
  }

  /**
   * Exception handler for all `Fault` implementations defined above. This is used for handling erroneous request
   * processing caused by the routes execution in  `akka-http`.
   */
  val Handler: PartialFunction[Throwable, Route] = {
    case LocationNotSupported      => complete(NotFound -> LocationNotSupported.error)
    case MissingWeatherInformation => complete(NotFound -> MissingWeatherInformation.error)
    case _                         => complete(InternalServerError -> InternalServiceError.error)
  }
}