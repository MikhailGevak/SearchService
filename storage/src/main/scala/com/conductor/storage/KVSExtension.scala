package com.conductor.storage

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.google.common.collect.{HashMultimap, Multimaps}

import scala.collection.JavaConverters._

trait KVS extends Extension {
  def put(key: String, value: String): Unit
  def get(key: String): List[String]
}

class KVSImpl extends KVS {
  private[this] val storage = Multimaps.synchronizedMultimap(HashMultimap.create[String, String]())

  def put(key: String, value: String) = storage.put(key, value)
  def get(key: String) = storage.get(key).asScala.toList
}

object KVSExtension extends ExtensionId[KVS] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): KVS = new KVSImpl
  override def lookup() = KVSExtension
}