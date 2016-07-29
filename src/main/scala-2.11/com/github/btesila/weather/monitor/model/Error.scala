package com.github.btesila.weather.monitor.model

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

/**
 * Type used for describing the service errors.
 *
 * @param errorCode Error code. Typically this will be a HTTP error code.
 * @param errorType A unique name for the error.
 * @param errorDescription A human readable error description.
 */
case class Error(errorCode: String, errorType: String, errorDescription: Option[String] = None)
object Error {
  implicit val ErrorFormat: RootJsonFormat[Error] = jsonFormat3(Error.apply)
}
