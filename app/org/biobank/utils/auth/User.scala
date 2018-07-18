package org.biobank.utils.auth

import com.mohiva.play.silhouette.api.Identity
import org.biobank.domain.users.{User => DomainUser}

final case class User(user: DomainUser) extends Identity
