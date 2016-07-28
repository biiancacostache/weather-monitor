package com.github.btesila.weather.monitor

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete => _, get => _, handleExceptions => _, path => _, _}
import akka.http.scaladsl.server.directives.ExecutionDirectives._
import akka.http.scaladsl.server.directives.MethodDirectives._
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.stream.ActorMaterializer
import com.github.btesila.weather.monitor.routes.WeatherMonitorRoutes

import scala.concurrent.Future

class RestService()(implicit system: ActorSystem, mat: ActorMaterializer) {

  val ping = (path("ping") & get) {
    complete("OK")
  }

  val routes = WeatherMonitorRoutes(WeatherMonitorOps()).routes

  def start(): Future[Http.ServerBinding] = {
    val handler = logRequestResult("req/resp", Logging.DebugLevel) {
      handleExceptions(Fault.Handler) {
        ping ~ routes
      }
    }
    val httpSettings = Settings(system).WeatherMonitor.Acceptor
    Http().bindAndHandle(
      handler = handler,
      interface = httpSettings.Host,
      port = httpSettings.Port
    )
  }
}

object RestService {
  def apply()(implicit system: ActorSystem, mat: ActorMaterializer) = new RestService()
}