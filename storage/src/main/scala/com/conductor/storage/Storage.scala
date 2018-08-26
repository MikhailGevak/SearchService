package com.conductor.storage

import akka.actor.{ActorRef, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.pattern.ask
import akka.util.Timeout
import com.conductor.storage.StorageNodeActor.{Entity, Get, Put}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

trait Storage {
  def putDocument(key: String, document: String): Unit

  def getDocument(key: String): Future[Option[String]]

  def search(tokens: Set[String]): Future[Set[String]]
}

class StorageImpl(private[storage] val storageProvider: ActorRef)(implicit ex: ExecutionContext)
  extends Storage with Extension {
  implicit val timeout: Timeout = 5.seconds

  def putDocument(key: String, document: String): Unit = {
    val putCommands = Put(s"key.$key", document) :: document.split("\\s+").toList.map { token =>
      Put(s"token.$token", key)
    }
    putCommands map { command => storageProvider ! command }
  }

  def getDocument(key: String): Future[Option[String]] = {
    val get = Get(s"key.$key")
    storageProvider.ask(get).collect { case Entity(key, values) => values.lastOption }
  }

  def search(tokens: Set[String]): Future[Set[String]] = {
    Future.sequence(tokens.par.map { token =>
      storageProvider.ask(Get(s"token.$token"))
    }.toList) map (_.collect { case Entity(_, values) => values.toSet }.reduce(_.intersect(_)))
  }
}

object StorageExtension
  extends ExtensionId[StorageImpl]
    with ExtensionIdProvider {
  override def lookup = StorageExtension

  //This method will be called by Akka
  // to instantiate our Extension
  override def createExtension(system: ExtendedActorSystem) = {
    val (systems, storageProvider) = StorageApp.startCluster(s"${system.name}-document_storage")

    system registerOnTermination {
      systems foreach {
        _.terminate()
      }
    }

    new StorageImpl(storageProvider)(system.dispatcher)
  }
}

package object storage {
  def id(key: Object, shardCount: Int) = (key.hashCode % shardCount).toString
}