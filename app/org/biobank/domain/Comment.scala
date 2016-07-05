package org.biobank.domain

import org.biobank.domain.user.UserId

import org.joda.time.DateTime

/** Users can add comments to entities.
  *
  * @param userId The ID of the user who made the comment.
  *
  * @param message The text entered by the user.
  *
  * @param timeAdded The date and time the comment was made.
  */
final case class Comment(userId: UserId, message: String, timeAdded: DateTime)
