package com.conductor
package storage

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

import collection.JavaConverters._

object StorageApp {
  val STORAGE_NODE_ROLE = "storage"
  val globalConfig = ConfigFactory.load()

  private[this] def startStorageNode(system: ActorSystem): ActorRef = system.actorOf(StorageNodeActor.props(),"Storage")

  def startCluster(systemName: String, ports: Seq[String] = globalConfig.getStringList("storage.ports").asScala): (Seq[ActorSystem], ActorRef) = {
    val (systems, storageNodes) = ports.map{ port =>
      val config = ConfigFactory.parseString(
        s"""akka.remote.netty.tcp.port=$port
           |akka.cluster.roles=[$STORAGE_NODE_ROLE]
         """.stripMargin)
        .withFallback(globalConfig)

      // Create an Akka system
      val system = ActorSystem(systemName, config)

      val storageNode = startStorageNode(system)
      (system, storageNode)
    }.unzip

    val leader = systems.head
    val cluster = Cluster(leader)

    systems foreach {system =>
      val address = Cluster(system).selfAddress
      cluster.join(address)
    }

    val storage = leader.actorOf(StorageProviderActor.props(storageNodes.toVector),
      leader.settings.config.getString("storage.actor")
    )
    (systems, storage)
  }
}
