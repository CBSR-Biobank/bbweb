package service

import service.study.{
  StudyAggregateComponent,
  StudyAggregateComponentImpl,
  StudyServiceComponent,
  StudyServiceComponentImpl,
  CollectionEventTypeServiceComponent,
  SpecimenGroupServiceComponent,
  CeventAnnotationTypeServiceComponent,
  ParticipantAnnotationTypeServiceComponent,
  SpecimenLinkAnnotationTypeServiceComponent
}
import domain.{ RepositoryComponent, RepositoryComponentImpl }

trait ProcessorComponent extends StudyAggregateComponent
  with UserAggregateComponent
  with RepositoryComponent

trait ProcessorComponentImpl extends ProcessorComponent
  with StudyAggregateComponentImpl
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