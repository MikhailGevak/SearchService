package com.conductor.rest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.conductor.storage.StorageExtension
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object RestApp extends App with RestRoute {
  override lazy val documentService = new DocumentServiceImpl(StorageExtension(system))
  val config = ConfigFactory.load()
  val host = config.getString("http.host")

  implicit val system: ActorSystem = ActorSystem("document-management-service")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(10.seconds)
  val port = config.getInt("http.port")

  Http().bindAndHandle(handler = route, interface = host, port = port) map { binding =>
    println(s"REST interface bound to ${binding.localAddress}")
  } recover { case ex =>
    println(s"REST interface could not bind to $host:$port", ex.getMessage)
  }
}
