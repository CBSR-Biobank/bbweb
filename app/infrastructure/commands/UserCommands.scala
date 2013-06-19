package infrastructure.commands

case class AddUserCmd(
  name: String,
  email: String,
  password: String,
  hasher: String,
  salt: Option[String],
  avatarUrl: Option[String])
