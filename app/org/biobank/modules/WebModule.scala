package org.biobank.modules

import org.biobank.controllers._
import org.biobank.controllers.study._
import org.biobank.controllers.centres._

import scaldi.Module
import scala.concurrent.duration._
import akka.util.Timeout

class WebModule extends Module {

  binding to new org.biobank.controllers.Application
  binding to new UsersController
  binding to new CeventAnnotTypeController
  binding to new CeventTypeController
  binding to new ParticipantAnnotTypeController
  binding to new ProcessingTypeController
  binding to new SpecimenGroupController
  binding to new SpecimenLinkAnnotTypeController
  binding to new SpecimenLinkTypeController
  binding to new StudiesController
  binding to new CentresController

  binding to play.api.libs.concurrent.Akka.system(inject[play.api.Application])

  binding identifiedBy 'akkaTimeout to Timeout(10 seconds)

}
