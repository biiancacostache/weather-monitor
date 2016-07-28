package com.github.btesila.weather.monitor

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.btesila.weather.monitor.Fault.{InternalServiceError, LocationNotSupported, MissingWeatherInformation}
import com.github.btesila.weather.monitor.model.Error
import org.scalatest.{Matchers, WordSpecLike}
import spray.json.RootJsonFormat
import spray.json._

import scala.concurrent.Future

class FaultSpec extends WordSpecLike with Matchers with ScalatestRouteTest {
  private def failWith(fault: Throwable) =
    handleExceptions(Fault.Handler) {
      complete(Future.failed[String](fault))
    }

  private def entity[A : RootJsonFormat](data: A): HttpEntity =
    HttpEntity(ContentTypes.`application/json`, data.toJson.prettyPrint)

  "The `LocationNotSupported` fault" should {
    "return the correct error object" in {
      val error = Error(
        NotFound.intValue.toString,
        "not_found",
        Some("This location is not supported.")
      )
      LocationNotSupported.error shouldBe error
    }
  }

  "The `MissingWeatherInformation` fault" should {
    "return the correct error object" in {
      val error = Error(
        NotFound.intValue.toString,
        "not_found",
        Some("Could not retrieve weather information for this location.")
      )
      MissingWeatherInformation.error shouldBe error
    }
  }

  "The `InternalServiceError` fault" should {
    "return the correct error object" in {
      val error = Error(
        InternalServerError.intValue.toString,
        "internal_server_error",
        Some("Internal server error")
      )
      InternalServiceError.error shouldBe error
    }
  }

  "The `Fault.Handler` exception handler" should {
    "handle a `LocationNotSupported` fault with a 404 Not Found response" in {
      Get() ~> failWith(LocationNotSupported) ~> check {
        status shouldBe NotFound
        responseEntity shouldBe entity(LocationNotSupported.error)
      }
    }
    "handle a `MissingWeatherInformation` fault with a 404 Not Found response" in {
      Get() ~> failWith(MissingWeatherInformation) ~> check {
        status shouldBe NotFound
        responseEntity shouldBe entity(MissingWeatherInformation.error)
      }
    }
    "handle an unknown fault with a 500 Internal Server Error" in {
      Get() ~> failWith(new RuntimeException) ~> check {
        status shouldBe InternalServerError
        responseEntity shouldBe entity(InternalServiceError.error)
      }
    }
  }
}
