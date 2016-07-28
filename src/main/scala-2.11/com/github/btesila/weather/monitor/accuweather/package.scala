package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import com.github.btesila.weather.monitor.Fault.{InternalServiceError, LocationNotSupported}

import scala.concurrent.{ExecutionContext, Future}

package object accuweather {
  /**
   *
   * @param request
   * @param system
   * @return
   */
  def prepareRequest(request: HttpRequest)(implicit system: ActorSystem): HttpRequest = {
    val apiKey = Settings(system).Accuweather.ApiKey
    val query = request.uri.query().+:("apikey" -> apiKey)
    request.withUri(request.uri.withQuery(query))
  }

  /**
   *
   * @param request
   * @param system
   * @param mat
   * @return
   */
  def send(request: HttpRequest)(implicit
    system: ActorSystem,
    mat: Materializer): Future[HttpResponse] = Http(system).singleRequest(prepareRequest(request))

  /**
   *
   * @param response
   * @param um
   * @param ec
   * @param mat
   * @tparam A
   * @return
   */
  def unmarshal[A](response: HttpResponse)(implicit
    um: FromEntityUnmarshaller[A],
    ec: ExecutionContext,
    mat: Materializer): Future[A] = um(response.entity)

  /**
   *
   * @param resp
   * @param expected
   * @param um
   * @param ec
   * @param mat
   * @return
   */
  def expect(resp: HttpResponse, expected: StatusCode, fault: Throwable)(implicit
    um: FromEntityUnmarshaller[String],
    ec: ExecutionContext,
    mat: Materializer): Future[HttpResponse] = {
    if (resp.status == expected) Future.successful(resp)
    else Future.failed(fault)
  }
}
