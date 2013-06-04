package domain

import akka.actor._

import scalaz._
import Scalaz._

abstract class Processor extends Actor {

  def process[T <: ConcurrencySafeEntity[_]](validation: DomainValidation[T])(onSuccess: T => Unit) = {
    validation.foreach { entity =>
      updateRepository(entity)
      onSuccess(entity)
    }
    sender ! validation
  }

  def updateRepository[T <: ConcurrencySafeEntity[_]](entity: T)

  def updateEntity[S <: ConcurrencySafeEntity[_], T <: ConcurrencySafeEntity[_]](entity: Option[S], id: Identity,
    expectedVersion: Option[Long])(f: S => DomainValidation[T]): DomainValidation[T] =
    entity match {
      case None => DomainError("no entity with id: %s" format id).fail
      case Some(entity) => for {
        current <- entity.requireVersion(expectedVersion)
        updated <- f(entity)
      } yield updated
    }

}