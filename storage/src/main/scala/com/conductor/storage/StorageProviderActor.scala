package com.conductor.storage

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import StorageNodeActor.StorageReq

object StorageProviderActor{
  def props(nodesStorages: Vector[ActorRef]): Props = Props(new StorageProviderActor(nodesStorages))

  private[storage] case object GetNodesStorages
  private[storage] case class NodesStorages(nodesStorages: Vector[ActorRef])

}

class StorageProviderActor(nodesStorages: Vector[ActorRef]) extends Actor with ActorLogging {
  import StorageProviderActor._

  val nodesCount = nodesStorages.size

  private[this] def nodeNumber(key: String) = Math.abs(key.hashCode % nodesCount)

  override def receive: Receive = {
    case req: StorageReq =>
      val n = nodeNumber(req.key)
      log.info(s"Forward request to ${n}th node")
      nodesStorages(n) forward req
    case GetNodesStorages =>
      sender() ! NodesStorages(nodesStorages)

  }
}