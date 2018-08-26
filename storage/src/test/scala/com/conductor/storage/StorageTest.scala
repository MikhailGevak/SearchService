package com.conductor.storage

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class StorageTest extends TestKit(ActorSystem("StorageTest")) with Matchers with FlatSpecLike with BeforeAndAfterAll {
  val storage = StorageExtension(system)

  it should "put Document into storage and get it by key" in {
    storage.putDocument("key", "document")
    val result = Await.result(storage.getDocument("key"), 5.seconds)
    result should be(Some("document"))
  }

  it should "put Document into storage and get it by token" in {
    storage.putDocument("key1", "token1 token2 token3")
    storage.putDocument("key2", "token2 token3")

    Await.result(storage.search(Set("token1")), 5.seconds) should be(Set("key1"))
    Await.result(storage.search(Set("token1", "token2")), 5.seconds) should be(Set("key1"))
    Await.result(storage.search(Set("token2", "token3")), 5.seconds) should be(Set("key1", "key2"))
    Await.result(storage.search(Set("token2")), 5.seconds) should be(Set("key1", "key2"))
  }

  override def afterAll(): Unit ={
    system.terminate()
  }
}
