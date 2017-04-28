package org.biobank.service.users

import org.biobank.fixture.TestFixture
import org.biobank.service.access.AccessService

class UsersServiceSpec extends TestFixture {

  val accessService = app.injector.instanceOf[AccessService]

}
