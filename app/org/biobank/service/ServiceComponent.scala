package org.biobank.service

import org.biobank.service.study.{
  StudyProcessorComponent,
  StudyProcessorComponentImpl,
  StudyServiceComponent,
  StudyServiceComponentImpl
}
import org.biobank.domain.{ RepositoryComponent, RepositoryComponentImpl }
import org.biobank.query.{ QueryComponent, QueryComponentImpl }

trait ProcessorComponent extends StudyProcessorComponent
  with UserProcessorComponent
  with RepositoryComponent

trait ProcessorComponentImpl extends ProcessorComponent
  with StudyProcessorComponentImpl
  with UserProcessorComponentImpl
  with RepositoryComponentImpl

trait ServiceComponent
  extends StudyServiceComponent
  with UserServiceComponent
  with ProcessorComponent
  with QueryComponent

trait ServiceComponentImpl
  extends ServiceComponent
  with StudyServiceComponentImpl
  with UserServiceComponentImpl
  with ProcessorComponentImpl
  with QueryComponentImpl
