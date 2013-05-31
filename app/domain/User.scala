package domain

import play.api.Play.current

case class User(email: String, name: String, password: String)

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("user.email") ~
      get[String]("user.name") ~
      get[String]("user.password") map {
        case email ~ name ~ password => User(email, name, password)
      }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[User] = {
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[User] = {
  }

  /**
   * Authenticate a User.
   */
  def authenticate(email: String, password: String): Option[User] = {
  }

  /**
   * Create a User.
   */
  def create(user: User): User = {
  }

}