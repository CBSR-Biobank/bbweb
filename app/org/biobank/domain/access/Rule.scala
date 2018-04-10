package org.biobank.domain.access

import org.biobank.domain.users.User

trait Rule {

  val name: String

  def execute(user: User, item: AccessItem): Boolean


}
