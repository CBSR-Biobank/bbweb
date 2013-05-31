package service.commands

case class AddUserCmd(name: String, email: String, password: String)
case class AuthenticateUserCmd(email: String, password: String)
