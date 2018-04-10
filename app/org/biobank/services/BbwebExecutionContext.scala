package org.biobank.services

import akka.actor.ActorSystem
import javax.inject._
import play.api.libs.concurrent.CustomExecutionContext

class BbwebExecutionContext @Inject()(actorSystem: ActorSystem)
 extends CustomExecutionContext(actorSystem, "bbweb-dispatcher")
