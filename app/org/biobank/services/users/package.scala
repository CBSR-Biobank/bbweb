package org.biobank.services

import play.api.libs.json._

package users {

  final case class UserCountsByStatus(total: Long, registeredCount: Long, activeCount: Long, lockedCount: Long)

  object UserCountsByStatus {

    implicit val userCountsByStatusWriter: Writes[UserCountsByStatus] = Json.writes[UserCountsByStatus]
  }

}
