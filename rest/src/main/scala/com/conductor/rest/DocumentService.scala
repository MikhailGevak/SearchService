package com.conductor
package rest

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.conductor.storage.Storage
import spray.json.DefaultJsonProtocol

import scala.concurrent.{ExecutionContext, Future}


object DocumentService {

  case class Added(key: String, document: String)

  case class Document(key: String, document: String)

  case class SearchResult(tokens: Set[String], keys: Set[String])

}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  import DocumentService._

  implicit val addedFormat = jsonFormat2(Added)
  implicit val documentFormat = jsonFormat2(Document)
  implicit val searchResultFormat = jsonFormat2(SearchResult)
}


trait DocumentService {

  import DocumentService._

  def put(key: String, document: String): Future[Added]

  def get(key: String): Future[Option[Document]]

  def search(tokens: Set[String]): Future[SearchResult]

  def search(tokens: String): Future[SearchResult] = search(tokens.split("\\s").toSet)
}

class DocumentServiceImpl(val storage: Storage)(implicit val system: ActorSystem) extends DocumentService {

  import DocumentService._

  implicit val ex: ExecutionContext = system.dispatcher

  def put(key: String, document: String) = Future.successful {
    storage.putDocument(key, document)
  } map { _ => Added(key, document) }

  def get(key: String) = storage.getDocument(key) map {
    _.map { document => Document(key, document) }
  }

  def search(tokens: Set[String]) = storage.search(tokens) map { keys => SearchResult(tokens, keys) }
}
