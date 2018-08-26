package com.conductor.storage

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.conductor.storage.StorageNodeActor.{Entity, Get, Put}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

class StorageNodeActorTest extends TestKit(ActorSystem("StorageNodeActorTest")) with ImplicitSender with Matchers with FlatSpecLike with BeforeAndAfterAll {
  it should "put value and get it" in {
    val storageNode = system.actorOf(StorageNodeActor.props)
    storageNode ! Put("key", "value")
    storageNode ! Get("key")
    expectMsg(Entity("key", List("value")))
  }

  it should "put 2 values and get a List" in {
    val storageNode = system.actorOf(StorageNodeActor.props)
    storageNode ! Put("key1", "value1")
    storageNode ! Put("key1", "value2")
    storageNode ! Get("key1")
    expectMsg(Entity("key1", List("value2", "value1")))
  }


  it should "get an Empty List for key which is not in DB" in {
    val storageNode = system.actorOf(StorageNodeActor.props)
    storageNode ! Get("key2")
    expectMsg(Entity("key2", Nil))
  }

  override def afterAll(): Unit = {
      system.terminate()
  }
}
