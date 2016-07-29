package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

package object accuweather {

  /**
   * Produces a new HttpRequest instance, based on the given one, by adding the required information to be sent to
   * the Accuweather service.
   *
   * @param request the initial request to be sent to Accuweather
   * @param system the required implicit actor system
   * @return a copy of the argument `HttpRequest` with all the necessary details required for the communication with the
   *         Accuweather service
   */
  def prepareRequest(request: HttpRequest)(implicit system: ActorSystem): HttpRequest = {
    val apiKey = Settings(system).Accuweather.ApiKey
    val query = request.uri.query().+:("apikey" -> apiKey)
    request.withUri(request.uri.withQuery(query))
  }

  /**
   * Sends the given HttpRequest to Accuweather, adding the required query parameter for service authorization, and
   * produces a future HttpResponse.
   *
   * @param request the `HttpRequest` to be sent to Accuweather
   * @param system the required implicit actor system.
   * @param mat the required implicit actor materializer
   * @return a future HttpResponse
   */
  def send(request: HttpRequest)(implicit
    system: ActorSystem,
    mat: Materializer): Future[HttpResponse] = Http(system).singleRequest(prepareRequest(request))

  /**
   * Unmarshals the given HttpResponse asynchronously into an `A` instance.
   *
   * @param response the HttpResponse to unmarshal
   * @param um the required implicit unmarshaller
   * @param ec the required implicit execution context for the asynchronous execution
   * @param mat the required implicit actor materializer
   * @tparam A the type of the result
   * @return the future instance of type `A`
   */
  def unmarshal[A](response: HttpResponse)(implicit
    um: FromEntityUnmarshaller[A],
    ec: ExecutionContext,
    mat: Materializer): Future[A] = um(response.entity)

  /**
   * Checks, asynchronously, if the status code of the given HttpResponse is the expected one. If this is not the case,
   * the operation will result into a failed future with the given Throwable.
   *
   * @param resp the analyzed HttpResponse
   * @param expected the expected Status Code
   * @param um the required implicit unmarshaller
   * @param ec the required implicit execution context for the asynchronous execution
   * @param mat the required implicit actor materializer
   * @return the same HttpResponse if the conditions are met
   */
  def expect(resp: HttpResponse, expected: StatusCode, fault: Throwable)(implicit
    um: FromEntityUnmarshaller[String],
    ec: ExecutionContext,
    mat: Materializer): Future[HttpResponse] = {
    if (resp.status == expected) Future.successful(resp)
    else Future.failed(fault)
  }
}
