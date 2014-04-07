package service

import service.study.{
  StudyProcessorComponent,
  StudyProcessorComponentImpl,
  StudyServiceComponent,
  StudyServiceComponentImpl,
  CollectionEventTypeServiceComponent,
  SpecimenGroupServiceComponent,
  CeventAnnotationTypeServiceComponent,
  ParticipantAnnotationTypeServiceComponent,
  SpecimenLinkAnnotationTypeServiceComponent
}
import domain.{ RepositoryComponent, RepositoryComponentImpl }

trait ProcessorComponent extends StudyProcessorComponent
  with UserAggregateComponent
  with RepositoryComponent

trait ProcessorComponentImpl extends ProcessorComponent
  with StudyProcessorComponentImpl
  with UserAggregateComponentImpl
  with CollectionEventTypeServiceComponent
  with SpecimenGroupServiceComponent
  with CeventAnnotationTypeServiceComponent
  with ParticipantAnnotationTypeServiceComponent
  with SpecimenLinkAnnotationTypeServiceComponent
  with RepositoryComponentImpl

trait ServiceComponent
  extends StudyServiceComponent
  with UserServiceComponent
  with ProcessorComponent
  with query.QueryComponent

trait ServiceComponentImpl
  extends ServiceComponent
  with StudyServiceComponentImpl
  with UserServiceComponentImpl
  with ProcessorComponentImpl
  with query.QueryComponentImpl