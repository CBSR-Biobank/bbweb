package service

import domain._

import akka.actor._
import scalaz._
import scalaz.Scalaz._

abstract class Processor extends Actor {

  private def process[T <: ConcurrencySafeEntity[_]](repositoryFunc: T => Unit)(validation: DomainValidation[T])(onSuccess: T => Unit) = {
    validation.foreach { entity =>
      repositoryFunc(entity)
      onSuccess(entity)
    }
    sender ! validation
  }

  def processUpdate[T <: ConcurrencySafeEntity[_]](repository: Repository[_, T])(validation: DomainValidation[T])(onSuccess: T => Unit) =
    process(repository.updateMap)(validation)(onSuccess)

  def processRemove[T <: ConcurrencySafeEntity[_]](repository: Repository[_, T])(validation: DomainValidation[T])(onSuccess: T => Unit) =
    process(repository.remove)(validation)(onSuccess)

}