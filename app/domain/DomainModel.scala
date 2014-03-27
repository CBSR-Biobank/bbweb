/**
 * This code taken from the articles:
 *
 * <ul>
 *  <li><a href="https://vaughnvernon.co/?p=780">DDD with Scala and Akka Revisited</a></li> *
 *  <li><a href="https://vaughnvernon.co/?p=770">Using Scala and Akka with Domain-Driven Design</a></li>
 * </ul>
 */
package domain

import akka.actor.{ Actor, ActorSystem, ActorContext, ActorRef, Props }

object DomainModel {
  def apply(name: String): DomainModel = {
    new DomainModel(name)
  }
}

class DomainModel(name: String) {
  val aggregateTypeRegistry = scala.collection.mutable.Map[String, AggregateType]()
  val system = ActorSystem(name)

  def aggregateOf(typeName: String, id: String): AggregateRef = {
    if (aggregateTypeRegistry.contains(typeName)) {
      val aggregateType = aggregateTypeRegistry(typeName)
      aggregateType.cacheActor ! RegisterAggregateId(id)
      AggregateRef(id, aggregateType.cacheActor)
    } else {
      throw new IllegalStateException("DomainModel type registry does not have a $typeName")
    }
  }

  def registerAggregateType(typeName: String): Unit = {
    if (!aggregateTypeRegistry.contains(typeName)) {
      val actorRef = system.actorOf(Props(new AggregateCache(typeName)), typeName)
      aggregateTypeRegistry(typeName) = AggregateType(actorRef)
    }
  }

  def shutdown() = {
    system.shutdown()
  }
}

class AggregateCache(typeName: String) extends Actor {
  val aggregateClass: Class[Actor] = Class.forName(typeName).asInstanceOf[Class[Actor]]
  val aggregateIds = scala.collection.mutable.Set[String]()

  def receive = {
    case message: CacheMessage =>
      val aggregate = context.child(message.id).getOrElse {
        if (!aggregateIds.contains(message.id)) {
          throw new IllegalStateException(s"No aggregate of type $typeName and id ${message.id}")
        } else {
          context.actorOf(new Props(aggregateClass), message.id)
        }
      }
      aggregate.tell(message.actualMessage, message.sender)

    case register: RegisterAggregateId =>
      this.aggregateIds.add(register.id)
  }
}

class AggregateCacheWorker(context: ActorContext) {
  val aggregateInfo = scala.collection.mutable.Map[String, Props]()

  def aggregateOf(props: Props, id: String): ActorRef = {
    if (!aggregateInfo.contains(id)) {
      aggregateInfo(id) = props
      context.actorOf(props, id)
    } else {
      throw new IllegalStateException(s"Aggregate with id $id already exists")
    }
  }

  def propsFor(id: String): Props = {
    if (aggregateInfo.contains(id)) {
      aggregateInfo(id)
    } else {
      throw new IllegalStateException(s"No Props for aggregate of id $id")
    }
  }
}

case class AggregateRef(id: String, cache: ActorRef) {
  def tell(message: Any)(implicit sender: ActorRef = null): Unit = {
    cache ! CacheMessage(id, message, sender)
  }

  def !(message: Any)(implicit sender: ActorRef = null): Unit = {
    cache ! CacheMessage(id, message, sender)
  }
}

case class AggregateType(cacheActor: ActorRef)

case class CacheMessage(id: String, actualMessage: Any, sender: ActorRef)

case class RegisterAggregateId(id: String)

case class ProvideWorker()
