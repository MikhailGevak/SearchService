package com.conductor.storage

import akka.actor.{Actor, ActorLogging, Props}

object StorageNodeActor{
  def props(): Props = Props(new StorageNodeActor("IndexInstance", "IndexMapInstance"))

  trait StorageReq{
    def key: String
  }
  case class Put(key: String, value: String) extends StorageReq
  case class Get(key: String) extends StorageReq

  case class Entity(key: String, values: Iterable[String])
}

class StorageNodeActor(hazelcastInstanceName: String, hazelcastMapInstanceName: String) extends Actor with ActorLogging{
  import StorageNodeActor._

  val storage = KVSExtension(context.system)

  override def receive: Receive = {
    case Put(key, value) =>
      storage.put(key, value)
      log.info(s"Put entity: key=${key}, value=${value}")
    case Get(key) =>
      val values = storage.get(key)
      log.info(s"Find entity: key=${key}, value=$values")
      sender() ! Entity(key, values)
  }
}
