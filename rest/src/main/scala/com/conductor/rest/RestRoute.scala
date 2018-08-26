package com.conductor.rest

import akka.http.scaladsl.server.Directives.{as, complete, decodeRequest, entity, get, parameters, path, put, _}
import akka.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait RestRoute extends JsonSupport {
  implicit val executionContext: ExecutionContext
  val route: Route =
    path("document" / Segment) { key =>
      get {
        rejectEmptyResponse {
          complete {
            val res = documentService.get(key)
            res
          }
        }
      } ~
        put {
          decodeRequest {
            entity(as[String]) { document =>
              complete {
                documentService.put(key, document)
              }
            }
          }
        }
    } ~
      path("search") {
        parameters('query.as[String]) { query =>
          complete {
            documentService.search(query)
          }
        }
      }

  def documentService: DocumentService
}
