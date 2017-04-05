package org.biobank.service

import akka.actor.ActorRef
import akka.util.Timeout
import scala.concurrent.duration._

trait BbwebService {

}

trait BbwebServiceImpl {

  implicit val timeout: Timeout = 5.seconds

  val processor: ActorRef

}
