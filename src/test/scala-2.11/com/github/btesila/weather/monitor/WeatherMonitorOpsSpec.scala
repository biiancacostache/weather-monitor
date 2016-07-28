package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.github.btesila.weather.monitor.Fault.{InternalServiceError, LocationNotSupported, MissingWeatherInformation}
import com.github.btesila.weather.monitor.WeatherMonitorFixture._
import com.github.btesila.weather.monitor.WeatherMonitorOpsSpec.TestableWeatherMonitorOps
import com.github.btesila.weather.monitor.accuweather.AccuWeatherClient
import com.github.btesila.weather.monitor.model.ActiveLocationRecord
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Future

class WeatherMonitorOpsSpec
  extends TestKit(ActorSystem())
  with ScalaFutures
  with WordSpecLike
  with Matchers
  with MockFactory {

  private val mat = ActorMaterializer()

  "The `getLocationCrtRecord` operation" should {
    "successfully provide an `ActiveLocationRecord` instance" in new TestableWeatherMonitorOps(system, mat) {
      override def fetchLocationCrtRecord(city: String, country: String): Future[ActiveLocationRecord] =
        Future.successful(activeLocationRecord)

      val result = getLocationCrtRecord(city, country).futureValue
      result.location shouldBe location
      result.crtConditions shouldBe crtConditions
    }
    "fail" when {
      "the location is not supported" in new TestableWeatherMonitorOps(system, mat) {
        override def fetchLocationCrtRecord(city: String, country: String): Future[ActiveLocationRecord] =
          Future.failed(LocationNotSupported)

        this.getLocationCrtRecord(city, country).failed.futureValue shouldBe LocationNotSupported
      }
      "the current conditions were not provided" in new TestableWeatherMonitorOps(system, mat) {
        override def fetchLocationCrtRecord(city: String, country: String): Future[ActiveLocationRecord] =
          Future.failed(MissingWeatherInformation)

        getLocationCrtRecord(city, country).failed.futureValue shouldBe MissingWeatherInformation
      }
      "the communication with Accuweather fails" when {
        "fetching the location record" in new TestableWeatherMonitorOps(system, mat) {
          override lazy val awc = mock[AccuWeatherClient]
          (this.awc.getLocation _).expects(*, *).returns(Future.failed(InternalServiceError))

          getLocationCrtRecord(city, country).failed.futureValue shouldBe InternalServiceError
        }
        "fetching the current conditions" in new TestableWeatherMonitorOps(system, mat) {
          override lazy val awc = mock[AccuWeatherClient]
          (this.awc.getLocation _).expects(*, *).returns(Future.successful(Some(location)))
          (this.awc.getCurrentConditions _).expects(*).returns(Future.failed(InternalServiceError))

          getLocationCrtRecord(city, country).failed.futureValue shouldBe InternalServiceError
        }
      }
    }
  }

  "The `getDailyForecast` operation" should {
    "successfully provide the daily forecast information" in new TestableWeatherMonitorOps(system, mat) {
      override def getLocationCrtRecord(city: String, country: String) = Future.successful(activeLocationRecord)
      override lazy val awc = mock[AccuWeatherClient]
      (awc.getDailyForecast _).expects(*).returns(Future.successful(Some(dailyForecast)))

      getDailyForecast(city, country).futureValue shouldBe (activeLocationRecord, dailyForecast)
    }
    "fail" when {
      "the location current record could not be retrieved" in new TestableWeatherMonitorOps(system, mat) {
        override def getLocationCrtRecord(city: String, country: String) = Future.failed(LocationNotSupported)

        getDailyForecast(city, country).failed.futureValue shouldBe LocationNotSupported
      }
      "the daily forecast was not provided" in new TestableWeatherMonitorOps(system, mat) {
        override def getLocationCrtRecord(city: String, country: String) = Future.successful(activeLocationRecord)
        override lazy val awc = mock[AccuWeatherClient]
        (awc.getDailyForecast _).expects(*).returns(Future.successful(None))

        getDailyForecast(city, country).failed.futureValue shouldBe MissingWeatherInformation
      }
      "the communication with Accuweather has failed" in new TestableWeatherMonitorOps(system, mat) {
        override def getLocationCrtRecord(city: String, country: String) = Future.successful(activeLocationRecord)
        override lazy val awc = mock[AccuWeatherClient]
        (awc.getDailyForecast _).expects(*).returns(Future.failed(InternalServiceError))

        getDailyForecast(city, country).failed.futureValue shouldBe InternalServiceError
      }
    }
  }

  "The `getExtendedForecast` operation" should {
    "successfully provide the extended forecast information" in new TestableWeatherMonitorOps(system, mat) {
      override def getLocationCrtRecord(city: String, country: String) = Future.successful(activeLocationRecord)
      override lazy val awc = mock[AccuWeatherClient]
      (awc.getExtendedForecast _).expects(*).returns(Future.successful(extendedForecast))

      getExtendedForecast(city, country).futureValue shouldBe (activeLocationRecord, extendedForecast)
    }
    "fail" when {
      "the location current record could not be retrieved" in new TestableWeatherMonitorOps(system, mat) {
        override def getLocationCrtRecord(city: String, country: String) = Future.failed(LocationNotSupported)

        getExtendedForecast(city, country).failed.futureValue shouldBe LocationNotSupported
      }
      "the extended forecast was not provided" in new TestableWeatherMonitorOps(system, mat) {
        override def getLocationCrtRecord(city: String, country: String) = Future.successful(activeLocationRecord)
        override lazy val awc = mock[AccuWeatherClient]
        (awc.getExtendedForecast _).expects(*).returns(Future.failed(MissingWeatherInformation))

        getExtendedForecast(city, country).failed.futureValue shouldBe MissingWeatherInformation
      }
    }
  }
}

object WeatherMonitorOpsSpec {
  class TestableWeatherMonitorOps(val as: ActorSystem, materializer: ActorMaterializer) extends WeatherMonitorOps {
    override implicit val system: ActorSystem = as
    override implicit val mat: ActorMaterializer = materializer
  }
}


