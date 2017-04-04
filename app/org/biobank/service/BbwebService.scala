package org.biobank.service

import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.duration._

trait BbwebService {

  def snapshot(): Unit

}

trait BbwebServiceImpl {

  implicit val timeout: Timeout = 5.seconds

  val processor: ActorRef

  def snapshot(): Unit = processor ! "snap"

}
