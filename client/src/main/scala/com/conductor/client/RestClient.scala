package com.conductor
package client

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.conductor.client.RestClient.{GetDocumentRequest, PutDocumentRequest, SearchRequest}
import spray.json.DefaultJsonProtocol
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import scala.concurrent.{ExecutionContext, Future}

object RestClient {

  trait RestRequest {
    def httpMethod: HttpMethod

    def httpEntity: RequestEntity

    def path: String
  }

  case class PutDocumentRequest(key: String, document: String) extends RestRequest {
    lazy val httpMethod = HttpMethods.PUT
    lazy val path = s"/document/$key"
    lazy val httpEntity = HttpEntity(document)
  }

  case class GetDocumentRequest(key: String) extends RestRequest {
    lazy val httpMethod = HttpMethods.GET
    lazy val path = s"/document/$key"
    lazy val httpEntity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`)
  }

  case class SearchRequest(tokens: Set[String]) extends RestRequest {
    lazy val httpMethod = HttpMethods.GET
    lazy val path = s"/document/search?query=${tokens.mkString(" ")}"
    lazy val httpEntity = HttpEntity.empty(ContentTypes.`text/plain(UTF-8)`)
  }

  private[client] def createHttpRequest(host: String, port: Int)(restRequest: RestRequest) = {
    new HttpRequest(
      method = restRequest.httpMethod,
      uri = Uri.from(scheme = "http", host = host, port = port, path = restRequest.path),
      entity = restRequest.httpEntity,
      headers = Nil,
      protocol = HttpProtocols.`HTTP/1.1`)
  }
}


class RestClient(host: String, port: Int)(httpClient: HttpClient)(implicit ex: ExecutionContext, mat: Materializer) extends Client with DefaultJsonProtocol with SprayJsonSupport {
  import RestClient._

  case class Document(key: String, document: String)
  case class SearchResult(tokens: Set[String], keys: Set[String])

  implicit val formatter1 = jsonFormat2(Document)
  implicit val formatter2 = jsonFormat2(SearchResult)

  private[this] val httpRequest = createHttpRequest(host, port)(_)

  def putDocument(key: String, document: String) = sendRequest(PutDocumentRequest(key, document)){_ =>
    Future.successful(())
  }

  def getDocument(key: String) = {
    def unmarshall(httpResponse: HttpResponse): Future[Option[String]] = {
      httpResponse match {
        case response if response.status == StatusCodes.NotFound => Future.successful(None)
        case response if response.status == StatusCodes.OK => Unmarshal(httpResponse.entity).to[Document] map {case Document(_, document)=> Some(document)}
      }

    }
    sendRequest(GetDocumentRequest(key)){unmarshall}
  }

  def searchDocument(tokens: Set[String]) = {
    def unmarshall(httpResponse: HttpResponse): Future[Set[String]] = Unmarshal(httpResponse.entity).to[SearchResult] map {_.keys}
    sendRequest(SearchRequest(tokens)){unmarshall}
  }

  private[this] def sendRequest[T](restRequest: RestRequest)(response: HttpResponse => Future[T]) = httpClient.sendRequest(httpRequest(restRequest))
    .flatMap{response(_)}
}
