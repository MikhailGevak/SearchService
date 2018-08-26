package com.conductor.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.Future

trait HttpClient{
  def sendRequest(request: HttpRequest): Future[HttpResponse]
}

class AkkaHttpClient(implicit system: ActorSystem) extends HttpClient{
  val http = Http()

  override def sendRequest(request: HttpRequest) = http.singleRequest(request)
}
