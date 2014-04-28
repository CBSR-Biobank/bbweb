package org.biobank.service

import org.biobank.service.study.{
  CollectionEventTypeProcessorComponent,
  StudyProcessorComponent,
  StudyServiceComponent,
  StudyServiceComponentImpl
}
import org.biobank.domain.{ RepositoryComponent, RepositoryComponentImpl }
import org.biobank.query.{ QueryComponent, QueryComponentImpl }

trait ProcessorComponent extends StudyProcessorComponent
    with UserProcessorComponent
    with RepositoryComponent

trait ProcessorComponentImpl extends ProcessorComponent
    with StudyProcessorComponent
    with UserProcessorComponent
    with RepositoryComponentImpl

trait ServiceComponent
    extends StudyServiceComponent
    with UserServiceComponent
    with ProcessorComponent
    with QueryComponent

trait ServiceComponentImpl
    extends ServiceComponent
    with StudyServiceComponentImpl
    with UserServiceComponent
    with ProcessorComponentImpl
    with QueryComponentImpl
