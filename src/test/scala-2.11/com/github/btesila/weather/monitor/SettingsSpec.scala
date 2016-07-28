package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{Matchers, WordSpecLike}

class SettingsSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers {
  "The `Settings` extension" should {
    "provide the appropriate Accuweather settings" in {
      val settings = Settings(system).Accuweather
      settings.ApiKey shouldBe "WTUr8dnrpCGYNnwDMDyv42qU9bxsAxjv"
      settings.LocationUri shouldBe "https://dataservice.accuweather.com/locations/v1/search"
      settings.CurrentConditionUri shouldBe "https://dataservice.accuweather.com/currentconditions/v1"
      settings.DailyForecastUri shouldBe "https://dataservice.accuweather.com/forecasts/v1/daily/1day"
      settings.ExtendedForecastUri shouldBe "https://dataservice.accuweather.com/forecasts/v1/daily/5day"
    }
    "provide the appropriate Http settings" in {
      val settings = Settings(system).WeatherMonitor.Acceptor
      settings.Host shouldBe "localhost"
      settings.Port shouldBe 8140
    }
  }
}
