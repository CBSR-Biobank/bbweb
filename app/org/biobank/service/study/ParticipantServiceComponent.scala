package org.biobank.service.study

import org.biobank.domain.study.ParticipantAnnotationType

import org.biobank.infrastructure.command._
import org.biobank.infrastructure.event._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.participant._

import scala.concurrent.Future

import scalaz._
import scalaz.Scalaz._

trait ParticipantServiceComponent {

  val participantService: ParticipantService

  trait ParticipantService extends ApplicationService {

    def getForStudy: Set[Participant]
  }

}