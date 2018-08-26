package com.conductor
package client

import akka.http.scaladsl.model._
import com.conductor.client.RestClient.{PutDocumentRequest, RestRequest}
import akka.stream.Materializer

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

object RestClient {

  trait RestRequest {
    def httpMethod: HttpMethod

    def httpEntity: RequestEntity

    def path: String
  }

  case class PutDocumentRequest(key: String, document: String) extends RestRequest {
    lazy val httpMethod = HttpMethods.PUT
    lazy val path = s"document/$key"
    lazy val httpEntity = HttpEntity(document)
  }

  case class GetDocumentRequest(key: String) extends RestRequest {
    lazy val httpMethod = HttpMethods.GET
    lazy val path = s"document/$key"
    lazy val httpEntity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`)
  }

  case class SearchRequest(tokens: Set[String]) extends RestRequest {
    lazy val httpMethod = HttpMethods.GET
    lazy val path = s"document/search?query=${tokens.mkString(" ")}"
    lazy val httpEntity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`)
  }

  private[client] def createHttpRequest(host: String, port: Int)(restRequest: RestRequest) = {
    new HttpRequest(
      method = restRequest.httpMethod,
      uri = Uri().withHost(host).withPort(port).withPath(Uri.Path(restRequest.path)),
      entity = restRequest.httpEntity,
      headers = Nil,
      protocol = HttpProtocols.`HTTP/1.1`)
  }
}

class RestClient(host: String, port: Int)(httpClient: HttpClient)(implicit ex: ExecutionContext, mat: Materializer) {

  import RestClient._

  val httpRequest = createHttpRequest(host, port)

  def putDocument(key: String, document: String) = sendRequest(PutDocumentRequest(key, document)){_ =>
    Future.successful(())
  }

  def getDocument(key: String) = sendRequest(GetDocumentRequest(key)){response => }

  def searchDocument(tokens: Set[String]) = sendRequest(SearchRequest(tokens))

  private[this] def sendRequest[T](restRequest: RestRequest)(response: HttpResponse => Future[T]) = httpClient.sendRequest(httpRequest(restRequest))
    .flatMap{response(_)}
}
