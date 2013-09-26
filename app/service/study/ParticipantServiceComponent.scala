package service.study

import domain.study.ParticipantAnnotationType

import service.commands._
import service.events._
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