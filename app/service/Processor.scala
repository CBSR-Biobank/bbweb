package service

import infrastructure._
import domain._
import akka.actor._
import scalaz._
import scalaz.Scalaz._

trait Processor extends Actor {

  protected def process(validation: DomainValidation[_]) =
    sender ! validation

}