package org.biobank.domain.study

trait StudyRepositoriesComponent
    extends StudyRepositoryComponent
    with SpecimenGroupRepositoryComponent
    with CollectionEventAnnotationTypeRepositoryComponent
    with CollectionEventTypeRepositoryComponent
    with ParticipantAnnotationTypeRepositoryComponent
    with ProcessingTypeRepositoryComponent
    with SpecimenLinkAnnotationTypeRepositoryComponent
    with SpecimenLinkTypeRepositoryComponent

trait StudyRepositoriesComponentImpl
    extends StudyRepositoryComponentImpl
    with SpecimenGroupRepositoryComponentImpl
    with CollectionEventAnnotationTypeRepositoryComponentImpl
    with CollectionEventTypeRepositoryComponentImpl
    with ParticipantAnnotationTypeRepositoryComponentImpl
    with ProcessingTypeRepositoryComponentImpl
    with SpecimenLinkAnnotationTypeRepositoryComponentImpl
    with SpecimenLinkTypeRepositoryComponentImpl
