package org.biobank.service

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.Timeout
import akka.actor.Props
import akka.persistence._

/**
 * Uses the Scala Cake Pattern to configure the application.
 */
trait TopComponent extends ServicesComponent {

  val studiesProcessor: ActorRef
  val centresProcessor: ActorRef
  val usersProcessor: ActorRef

  val studiesService: StudiesService
  val centresService: CentresService
  val usersService: UsersService

}
