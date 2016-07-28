package com.github.btesila.weather.monitor.routes

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.github.btesila.weather.monitor.Fault.{InternalServiceError, LocationNotSupported}
import com.github.btesila.weather.monitor.WeatherMonitorFixture._
import com.github.btesila.weather.monitor.{Fault, WeatherMonitorOps}
import com.github.btesila.weather.monitor.model.WeatherMonitorProtocol
import com.github.btesila.weather.monitor.routes.WeatherMonitorRoutesSpec.TestableWeatherMonitorRoutes
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpecLike}
import spray.json.{RootJsonFormat, _}

import scala.concurrent.Future

class WeatherMonitorRoutesSpec
  extends WordSpecLike
  with Matchers
  with ScalatestRouteTest
  with MockFactory
  with WeatherMonitorProtocol {

  private def prepareReq(req: HttpRequest, city: String, country: String): HttpRequest =
    req.withUri(req.uri.withQuery(Query("city" -> city, "country" -> country)))

  private def entity[A : RootJsonWriter](data: A): HttpEntity.Strict =
    HttpEntity(ContentTypes.`application/json`, data.toJson.prettyPrint)

  private val ops = mock[WeatherMonitorOps]
  private val routes = new TestableWeatherMonitorRoutes(ops)

  "The WeatherMonitorRoutes" when {
    "receiving GET on `weather/current/conditions" should {
      val req = prepareReq(Get("http://localhost:8140/weather/current/conditions"), city, country)
      "return 200" in {
        (ops.getLocationCrtRecord _).expects(*, *).returns(Future.successful(activeLocationRecord))
        req ~> routes.routes ~> check {
          status shouldBe StatusCodes.OK
          responseEntity shouldBe entity(activeLocationRecord)
        }
      }
      "return 404" in {
        (ops.getLocationCrtRecord _).expects(*, *).returns(Future.failed(LocationNotSupported))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.NotFound
          responseEntity shouldBe entity(LocationNotSupported.error)
        }
      }
      "return 500" in {
        (ops.getLocationCrtRecord _).expects(*, *).returns(Future.failed(new RuntimeException))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.InternalServerError
          responseEntity shouldBe entity(InternalServiceError.error)
        }
      }
    }
    "receiving GET on `weather/today" should {
      val req = prepareReq(Get("http://localhost:8140/weather/today"), city, country)
      "return 200" in {
        (ops.getDailyForecast _).expects(*, *).returns(Future.successful((activeLocationRecord, dailyForecast)))
        req ~> routes.routes ~> check {
          status shouldBe StatusCodes.OK
          responseEntity shouldBe entity((activeLocationRecord, dailyForecast))
        }
      }
      "return 404" in {
        (ops.getDailyForecast _).expects(*, *).returns(Future.failed(LocationNotSupported))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.NotFound
          responseEntity shouldBe entity(LocationNotSupported.error)
        }
      }
      "return 500" in {
        (ops.getDailyForecast _).expects(*, *).returns(Future.failed(new RuntimeException))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.InternalServerError
          responseEntity shouldBe entity(InternalServiceError.error)
        }
      }
    }
    "receiving GET on `weather/forecast" should {
      val req = prepareReq(Get("http://localhost:8140/weather/forecast"), city, country)
      "return 200" in {
        (ops.getExtendedForecast _).expects(*, *).returns(Future.successful((activeLocationRecord, extendedForecast)))
        req ~> routes.routes ~> check {
          status shouldBe StatusCodes.OK
          responseEntity shouldBe entity((activeLocationRecord, extendedForecast))
        }
      }
      "return 404" in {
        (ops.getExtendedForecast _).expects(*, *).returns(Future.failed(LocationNotSupported))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.NotFound
          responseEntity shouldBe entity(LocationNotSupported.error)
        }
      }
      "return 500" in {
        (ops.getExtendedForecast _).expects(*, *).returns(Future.failed(new RuntimeException))
        req ~> handleExceptions(Fault.Handler) { routes.routes  }~> check {
          status shouldBe StatusCodes.InternalServerError
          responseEntity shouldBe entity(InternalServiceError.error)
        }
      }
    }
  }
}

object WeatherMonitorRoutesSpec {
  class TestableWeatherMonitorRoutes(op: WeatherMonitorOps) extends WeatherMonitorRoutes {
    override def ops: WeatherMonitorOps = op
  }
}