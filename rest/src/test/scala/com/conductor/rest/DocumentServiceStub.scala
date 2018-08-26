package com.conductor.rest

import com.conductor.rest.DocumentService.{Added, Document, SearchResult}
import scala.concurrent.Future

case class DocumentServiceStub(putRes: Added, getRes: Option[Document], searchRes: SearchResult) extends DocumentService {

  import DocumentService._

  override def put(key: String, document: String): Future[Added] = Future.successful(putRes)

  override def get(key: String): Future[Option[Document]] = Future.successful(getRes)

  override def search(tokens: Set[String]): Future[SearchResult] = Future.successful(searchRes)
}
