package org.biobank.domain.access

import org.biobank.domain.user.User

trait Rule {

  val name: String

  def execute(user: User, item: AccessItem): Boolean


}
