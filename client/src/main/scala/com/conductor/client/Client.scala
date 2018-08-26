package com.conductor.client

import scala.concurrent.Future

trait Client{
  def putDocument(key: String, document: String): Future[Unit]
  def getDocument(key: String):  Future[Option[String]]
  def searchDocument(tokens: Set[String]): Future[Set[String]]
}
