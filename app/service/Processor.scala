package service

import domain._

import akka.actor._
import scalaz._
import scalaz.Scalaz._

abstract class Processor extends Actor {

  private def process[T <: ConcurrencySafeEntity[_]](validation: DomainValidation[T])(repositoryFunc: T => Unit)(onSuccess: T => Unit) = {
    validation.foreach { entity =>
      repositoryFunc(entity)
      onSuccess(entity)
    }
    sender ! validation
  }

  def processUpdate[T <: ConcurrencySafeEntity[_]](validation: DomainValidation[T], repository: Repository[_, T])(onSuccess: T => Unit) =
    process(validation)(repository.updateMap)(onSuccess)

  def processRemove[T <: ConcurrencySafeEntity[_]](validation: DomainValidation[T], repository: Repository[_, T])(onSuccess: T => Unit) =
    process(validation)(repository.remove)(onSuccess)

  def updateEntity[S <: ConcurrencySafeEntity[_], T <: ConcurrencySafeEntity[_]](entity: Option[S],
    id: domain.Identity, expectedVersion: Option[Long])(f: S => DomainValidation[T]): DomainValidation[T] =
    entity match {
      case None => DomainError("no entity with id: %s" format id).fail
      case Some(entity) => for {
        current <- entity.requireVersion(expectedVersion)
        updated <- f(entity)
      } yield updated
    }

}