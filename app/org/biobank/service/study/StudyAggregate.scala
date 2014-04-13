package org.biobank.service.study

import org.biobank.service.Aggregate
import org.biobank.infrastructure.event.StudyEvents._

import akka.event.Logging

class StudyAggregate extends Aggregate {

  val log = Logging(context.system, this)

  def receive = {
      case event: StudyAddedEvent => addStudy(cmd)
  }

}
