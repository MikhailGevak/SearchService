package com.conductor.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethod, HttpRequest, HttpResponse, Uri}
import akka.stream.{ActorMaterializer, Materializer}
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.concurrent.Future

case class HttpClientStub(val receive: PartialFunction[(HttpMethod, Uri, String), HttpResponse] ) extends HttpClient {

  implicit val system = ActorSystem("stubsystem")
  implicit val mat: Materializer = ActorMaterializer()

  def sendRequest(request: HttpRequest): Future[HttpResponse] = {
    request.entity.toStrict(1.second).map{strict =>
      receive((request.method, request.uri,strict.data.toString)) }
  }

}
