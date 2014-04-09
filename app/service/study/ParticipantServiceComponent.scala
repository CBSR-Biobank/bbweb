package service.study

import domain.study.ParticipantAnnotationType

import infrastructure.command._
import infrastructure.event._
import service._
import domain._
import domain.participant._

import scala.concurrent.Future

import scalaz._
import scalaz.Scalaz._

trait ParticipantServiceComponent {

  val participantService: ParticipantService

  trait ParticipantService extends ApplicationService {

    def getForStudy: Set[Participant]
  }

}