package com.conductor.storage

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.conductor.storage.StorageNodeActor.{Entity, Get, Put}
import com.conductor.storage.StorageProviderActor.{GetNodesStorages, NodesStorages}
import org.scalatest.{BeforeAndAfterEach, FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
class StorageProviderTest extends Matchers with FlatSpecLike with BeforeAndAfterEach {
  implicit val timeout: Timeout = 5.seconds

  val systemName = "StorageProviderTest"
  var storageProvider: ActorRef = _
  var storageNodes: Vector[ActorRef] = _
  var systems: Seq[ActorSystem] = _

  override def beforeEach(): Unit = {
    //start some ActorSystems and provider
    val (_systems, _storageProvider) = StorageApp.startCluster(systemName, Seq("0", "0", "0", "0"))
    systems = _systems
    storageProvider = _storageProvider

    storageNodes = Await.result(storageProvider.ask(GetNodesStorages), timeout.duration)
      .asInstanceOf[NodesStorages].nodesStorages
  }

  it should "put Document on one node only" in {

    storageProvider ! Put("key", "value")
    Thread.sleep(500)
    val results = storageNodes.flatMap { node =>
      Await.result(node.ask(Get("key")), timeout.duration).asInstanceOf[Entity].values.lastOption
    }

    results.size should be(1)
  }

  override def afterEach(): Unit = {
    systems foreach (_.terminate())
  }
}
