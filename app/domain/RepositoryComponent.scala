package domain

import domain.study.{
  CollectionEventAnnotationTypeRepositoryComponent,
  CollectionEventAnnotationTypeRepositoryComponentImpl,
  CollectionEventTypeRepositoryComponent,
  CollectionEventTypeRepositoryComponentImpl,
  StudyRepositoryComponent,
  StudyRepositoryComponentImpl,
  SpecimenGroupRepositoryComponent,
  SpecimenGroupRepositoryComponentImpl
}

trait RepositoryComponent
  extends StudyRepositoryComponent
  with SpecimenGroupRepositoryComponent
  with CollectionEventAnnotationTypeRepositoryComponent
  with CollectionEventTypeRepositoryComponent
  with UserRepositoryComponent

trait RepositoryComponentImpl
  extends RepositoryComponent
  with StudyRepositoryComponentImpl
  with SpecimenGroupRepositoryComponentImpl
  with CollectionEventAnnotationTypeRepositoryComponentImpl
  with CollectionEventTypeRepositoryComponentImpl
  with UserRepositoryComponentImpl