package domain

trait HasName { val name: String }
trait HasDescription { val description: String }
trait HasDescriptionOption { val description: Option[String] }
trait HasAddedBy { val addedBy: UserId }
trait HasTimeAdded { val timeAdded: Long }
trait HasUpdatedBy { val updatedBy: Option[UserId] }
trait HasTimeUpdated { val timeUpdated: Option[Long] }