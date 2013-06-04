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

}