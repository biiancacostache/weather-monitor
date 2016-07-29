package com.github.btesila.weather.monitor.model

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsArray, JsObject, JsonFormat, RootJsonFormat, _}

/**
 * Spray json protocol definitions for the types used by the service.
 */
trait WeatherMonitorProtocol {

  implicit val LocationFormat: RootJsonFormat[Location] = new RootJsonFormat[Location] {
    override def read(json: JsValue): Location = {
      val err = "Unable to deserialize into a Location instance."
      json match {
        case obj: JsObject =>
          val key     = getField[String](obj, err, "Key")
          val city    = getField[String](obj, err, "LocalizedName")
          val country = obj.getFields("Country") match {
            case Seq(c: JsObject) => getField[String](c, err, "LocalizedName")
            case _                => deserializationError(err)
          }
          Location(key, city, country)
        case _ => deserializationError(err)
      }
    }
    override def write(obj: Location): JsValue =
      JsObject(
        Map(
          "ciy"     -> JsString(obj.city),
          "country" -> JsString(obj.country)
        )
      )
  }

  implicit val TemperatureFormat: RootJsonFormat[Temperature] = new RootJsonFormat[Temperature] {
    override def read(json: JsValue): Temperature = {
      val err = "Unable to deserialize into a Temperature instance."
      json match {
        case obj: JsObject =>
          val value = getField[Int](obj, err, "Value")
          val unit = getField[String](obj, err, "Unit")
          Temperature(value, unit)
        case _ => deserializationError(err)
      }
    }
    override def write(obj: Temperature): JsValue =
      JsObject(
        Map(
          "value" -> JsNumber(obj.value),
          "unit"  -> JsString(obj.unit)
        )
      )
  }

  implicit val CurrentWeatherConditionsFormat: RootJsonFormat[CurrentWeatherConditions] = new RootJsonFormat[CurrentWeatherConditions] {
    override def read(json: JsValue): CurrentWeatherConditions = {
      val err = "Unable to deserialize into a CurrentWeatherConditions instance."
      json match {
        case obj: JsObject =>
          val dateTime    = getField[DateTime](obj, err, "LocalObservationDateTime")
          val description = getField[String](obj, err, "WeatherText")
          val isDayTime   = getField[Boolean](obj, err, "IsDayTime")
          val temperature = obj.getFields("Temperature") match {
            case Seq(t: JsObject) => getField[Temperature](t, err, "Metric")
            case _                => deserializationError(err)
          }
          CurrentWeatherConditions(dateTime, description, isDayTime, temperature)
        case _ => deserializationError(err)
      }
    }
    override def write(obj: CurrentWeatherConditions): JsValue =
      JsObject(
        Map(
          "date"        -> obj.dateTime.toJson,
          "headline"    -> JsString(obj.description),
          "isDayTime"   -> JsBoolean(obj.isDayTime),
          "temperature" -> obj.temperature.toJson
        )
      )
  }

  implicit val DailyForecastFormat: RootJsonFormat[DailyForecast] = new RootJsonFormat[DailyForecast] {
    override def read(json: JsValue): DailyForecast = {
      val err = "Unable to deserialize into a DailyForecast instance."
      json match {
        case obj: JsObject =>
          val date = getField[DateTime](obj, err, "Date")
          val (minimumTemperature, maximumTemperature) = obj.getFields("Temperature") match {
            case Seq(t: JsObject) => (getField[Temperature](t, err, "Minimum"), getField[Temperature](t, err, "Maximum"))
            case _                => deserializationError(err)
          }
          val day = obj.getFields("Day") match {
            case Seq(d: JsObject) => getField[String](d, err, "IconPhrase")
            case _                => deserializationError(err)
          }
          val night = obj.getFields("Night") match {
            case Seq(n: JsObject) => getField[String](n, err, "IconPhrase")
            case _                => deserializationError(err)
          }
          DailyForecast(date, minimumTemperature, maximumTemperature, day, night)
      }
    }
    override def write(obj: DailyForecast): JsValue = {
      val fields = Map(
        "date"               -> obj.dateTime.toJson,
        "minimumTemperature" -> obj.minimumTemperature.toJson,
        "maximumTemperature" -> obj.maximumTemperature.toJson,
        "day"                -> JsString(obj.dayDescription),
        "night"              -> JsString(obj.nightDescription)
      )
      JsObject(fields)
    }
  }

  implicit val ExtendedForecastFormat: RootJsonFormat[ExtendedForecast] = new RootJsonFormat[ExtendedForecast] {
    override def read(json: JsValue): ExtendedForecast = {
      val err = "Unable to deserialize into an ExtendedForecast instance."
      json match {
        case obj: JsObject =>
          val (description, startDate, endDate) = obj.getFields("Headline") match {
            case Seq(h: JsObject) => (
              getField[String](h, err, "Text"),
              getField[DateTime](h, err, "EffectiveDate"),
              getField[DateTime](h, err, "EndDate"))
            case _ => deserializationError(err)
          }
          val forecasts   = getField[List[DailyForecast]](obj, err, "DailyForecasts")
          ExtendedForecast(startDate, endDate, description, forecasts)
        case _ => deserializationError(err)
      }
    }
    override def write(obj: ExtendedForecast): JsValue = {
      val fields = Map(
        "startDate"      -> obj.startDateTime.toJson,
        "endDate"        -> obj.endDateTime.toJson,
        "description"    -> JsString(obj.description),
        "dailyForecasts" -> obj.dailyForecasts.toJson
      )
      JsObject(fields)
    }
  }

  implicit val CrtConditionsResponseWriter: RootJsonWriter[ActiveLocationRecord] = new RootJsonWriter[ActiveLocationRecord] {
    override def write(obj: ActiveLocationRecord): JsValue = {
      val (location, crtConditions) = (obj.location, obj.crtConditions)
      val locationFields = location.toJson.asJsObject.fields
      val crtConditionsFields = crtConditions.toJson.asJsObject.fields
      JsObject(locationFields ++ crtConditionsFields)
    }
  }

  implicit val DailyForecastResponseWriter: RootJsonWriter[(ActiveLocationRecord, DailyForecast)] =
    new RootJsonWriter[(ActiveLocationRecord, DailyForecast)] {
      override def write(obj: (ActiveLocationRecord, DailyForecast)): JsValue = {
        val (record, forecast) = obj
        val recordFields = record.toJson.asJsObject.fields
        val forecastFields =  forecast.toJson.asJsObject.fields
        JsObject(recordFields ++ forecastFields)
      }
  }

  implicit val ExtendedForecastResponseWriter: RootJsonWriter[(ActiveLocationRecord, ExtendedForecast)] =
    new RootJsonWriter[(ActiveLocationRecord, ExtendedForecast)] {
      override def write(obj: (ActiveLocationRecord, ExtendedForecast)): JsValue = {
        val (record, forecast) = obj
        val recordFields = record.toJson.asJsObject.fields
        val forecastFields =  forecast.toJson.asJsObject.fields
        JsObject(recordFields ++ forecastFields)
      }
    }

  implicit val DateTimeFormat: RootJsonFormat[DateTime] = new RootJsonFormat[DateTime] {
    private val parserISO : DateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis()
    override def read(json: JsValue) : DateTime = json match {
      case JsString(s) => parserISO.parseDateTime(s)
      case _           => deserializationError("Unable to deserialize into a DateTime instance.")
    }
    override def write(obj: DateTime) = JsString(parserISO.print(obj))
  }

  implicit def listFormat[T : RootJsonFormat] = new RootJsonFormat[List[T]] {
    override def read(json: JsValue): List[T] = json match {
      case JsArray(elements) => elements.map(_.convertTo[T])(collection.breakOut)
      case x                 => deserializationError("Unable to deserialize into a list instance.")
    }
    override def write(obj: List[T]): JsValue = JsArray(obj.map(_.toJson).toVector)
  }

  private def getField[A : JsonFormat](obj: JsObject, errorMsg: String, fields: String*) =
    obj.getFields(fields: _*) match {
      case Seq(value: JsValue) => value.convertTo[A]
      case _                   => deserializationError(errorMsg)
    }
}
