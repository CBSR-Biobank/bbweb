package org.biobank.service

import org.biobank.domain.{ DomainComponent, DomainComponentImpl }
import org.biobank.service.study.{
  CollectionEventTypeProcessorComponent,
  StudiesProcessorComponent,
  StudiesServiceComponent,
  StudiesServiceComponentImpl
}
import org.biobank.service.centre.{
  CentresProcessorComponent,
  CentresServiceComponent,
  CentresServiceComponentImpl
}
import org.biobank.query.{ QueryComponent, QueryComponentImpl }

trait ProcessorsComponent
    extends StudiesProcessorComponent
    with CentresProcessorComponent
    with UsersProcessorComponent
    with DomainComponent
    with DomainComponentImpl
    with PasswordHasherComponent
    with PasswordHasherComponentImpl

trait ServicesComponent
    extends StudiesServiceComponent
    with CentresServiceComponent
    with UsersServiceComponent
    with ProcessorsComponent
    with QueryComponent

trait ServicesComponentImpl
    extends ServicesComponent
    with CentresServiceComponentImpl
    with StudiesServiceComponentImpl
    with UsersServiceComponent
    with ProcessorsComponent
    with QueryComponentImpl
