package service

import domain._
import akka.actor._
import scalaz._
import scalaz.Scalaz._

trait Processor extends Actor {

  protected def process[T <: ConcurrencySafeEntity[_]](validation: DomainValidation[T]) =
    sender ! validation

}