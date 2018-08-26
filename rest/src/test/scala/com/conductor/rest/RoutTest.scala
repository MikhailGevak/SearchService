package com.conductor
package rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.conductor.rest.DocumentService._
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.ExecutionContext

class RoutTest extends Matchers with FlatSpecLike with ScalatestRouteTest {
  val added = Added("key", "document")
  val document = Document("key", "document")
  val searchResult = SearchResult(Set("token1", "token2"), Set("ke1", "key2", "key3"))

  val serviceStub = DocumentServiceStub(added, Some(document), searchResult)

  {
    val restRoute = new RestRoute {
      override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
      override val documentService: DocumentService = serviceStub
    }
    "The service" should "return and 'Added' response on Put request" in {
      Put("/document/key").withEntity("sfsf sfsff") ~> restRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[String] should be(restRoute.addedFormat.write(added).toString())
      }
    }

    it should "return and 'Document' response on Get request" in {
      Get("/document/key") ~> restRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[String] should be(restRoute.documentFormat.write(document).toString())
      }
    }

    it should "return and 'SearchResult' response Query request" in {
      Get("/search?query=query") ~> restRoute.route ~> check {
        response.status should be(StatusCodes.OK)
        responseAs[String] should be(restRoute.searchResultFormat.write(searchResult).toString())
      }
    }
  }
  {
    val restRoute = new RestRoute {
      override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
      override val documentService: DocumentService = serviceStub.copy(getRes = None)
    }
    it should "return 404 response on Get request if document can't be found" in {
      Get("/document/key") ~> Route.seal(restRoute.route) ~> check {
        response.status should be(StatusCodes.NotFound)
      }
    }
  }
}