package com.conductor
package client

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class RestClientTest extends Matchers with FlatSpecLike {
  implicit val system = ActorSystem("stubsystem")
  implicit val mat: Materializer = ActorMaterializer()

  val httpStub = HttpClientStub {
    case ( HttpMethods.GET, uri, _) if uri.toString.equals("http://localhost:8080/document/123") =>
        val response =
          """{
            |"key": "123",
            |"document": "mama was cleaning frame"
            |}
          """.stripMargin
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ByteString(response)))
    case ( HttpMethods.PUT, uri, body) if uri.toString.equals("http://localhost:8080/document/123") =>
      val response = s"""{
                       |"key": "123",
                       |"document": "$body"
                       |}""".stripMargin
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ByteString(response)))
    case ( HttpMethods.GET, uri, _) if uri.toString.equals("http://localhost:8080/document/search%3Fquery=hello%20world") =>
      val response =
        """{
          |"tokens": [
          |  "mama",
          |  "cleaning",
          |  "frame"
          |],
          |"keys": [
          |  "123"
          |]
          |}
        """.stripMargin
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, ByteString(response)))
  }

  val restClient = new RestClient("localhost", 8080)(httpStub)

  "RestClient" should "get document" in {
      val result = Await.result(restClient.getDocument("123"), 2.seconds)
      result should be(Some("mama was cleaning frame"))
  }

  it should "put document" in {
    val result = Await.result(restClient.putDocument("123", "Hello World!"), 2.seconds)
    result should be(())
  }

  it should "search tokens" in {
    val result = Await.result(restClient.searchDocument(Set("hello", "world")), 2.seconds)
    result should be(Set("123"))
  }
}
