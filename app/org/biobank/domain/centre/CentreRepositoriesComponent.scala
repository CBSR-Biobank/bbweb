package org.biobank.domain.centre

trait CentreRepositoriesComponent
    extends CentreRepositoryComponent
    with StudyCentreRepositoryComponent
    with CentreLocationRepositoryComponent

trait CentreRepositoriesComponentImpl
    extends CentreRepositoryComponentImpl
    with StudyCentreRepositoryComponentImpl
    with CentreLocationRepositoryComponentImpl
