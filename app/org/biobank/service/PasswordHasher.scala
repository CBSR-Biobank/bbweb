package org.biobank.service

import com.github.t3hnar.bcrypt._
import org.slf4j.LoggerFactory

trait PasswordHasherComponent {

  val passwordHasher: PasswordHasher

  trait PasswordHasher {
    def encrypt(password: String, salt: String): String

    def generateSalt: String

    def valid(encryptedPwd: String,  salt: String, enteredPwd: String): Boolean
  }

}

trait PasswordHasherComponentImpl
    extends PasswordHasherComponent {

  val passwordHasher: PasswordHasher = new PasswordHasherImpl

  class PasswordHasherImpl extends PasswordHasher {

    val log = LoggerFactory.getLogger(this.getClass)

    def encrypt(password: String, salt: String): String = {
      password.bcrypt(salt)
    }

    def generateSalt: String = {
      com.github.t3hnar.bcrypt.generateSalt
    }

    def valid(encryptedPwd: String, salt: String, enteredPwd: String): Boolean = {
      encryptedPwd == enteredPwd.bcrypt(salt)
    }
  }

}
