package service

import service.study.{
  StudyProcessorComponent,
  StudyProcessorComponentImpl,
  StudyServiceComponent,
  StudyServiceComponentImpl,
  CollectionEventTypeServiceComponent,
  SpecimenGroupServiceComponent,
  CeventAnnotationTypeServiceComponent,
  ParticipantAnnotationTypeServiceComponent
}
import domain.{ RepositoryComponent, RepositoryComponentImpl }

trait ProcessorComponent extends StudyProcessorComponent
  with UserProcessorComponent
  with RepositoryComponent

trait ProcessorComponentImpl extends ProcessorComponent
  with StudyProcessorComponentImpl
  with UserProcessorComponentImpl
  with CollectionEventTypeServiceComponent
  with SpecimenGroupServiceComponent
  with CeventAnnotationTypeServiceComponent
  with ParticipantAnnotationTypeServiceComponent
  with RepositoryComponentImpl

trait ServiceComponent extends StudyServiceComponent with UserServiceComponent
  with ProcessorComponent

trait ServiceComponentImpl extends ServiceComponent
  with StudyServiceComponentImpl
  with UserServiceComponentImpl
  with ProcessorComponentImpl