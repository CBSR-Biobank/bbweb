package org.biobank.domain

import org.biobank.domain.study.{ StudyRepositoriesComponent, StudyRepositoriesComponentImpl }
import org.biobank.domain.centre.{ CentreRepositoriesComponent, CentreRepositoriesComponentImpl }
import org.biobank.domain.user.{ UserRepositoryComponent, UserRepositoryComponentImpl }

trait RepositoriesComponent
    extends StudyRepositoriesComponent
    with CentreRepositoriesComponent
    with UserRepositoryComponent
    with LocationRepositoryComponent

trait RepositoriesComponentImpl
    extends RepositoriesComponent
    with StudyRepositoriesComponentImpl
    with CentreRepositoriesComponentImpl
    with UserRepositoryComponentImpl
    with LocationRepositoryComponentImpl
