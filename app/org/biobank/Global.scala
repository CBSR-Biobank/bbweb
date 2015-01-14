package org.biobank

import org.biobank.domain.user.{
  RegisteredUser,
  User,
  UserId,
  UserRepository
}
import org.biobank.service.PasswordHasher
import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import org.biobank.modules.{WebModule, UserModule}
import play.api.libs.concurrent.Akka
import play.api.Logger

import akka.actor.ActorSystem
import akka.actor.Props

import java.io.File
import org.joda.time.DateTime
import scaldi.play.ScaldiSupport
import scaldi.Module
import scaldi.akka.AkkaInjectable

/** This is a trait so that it can be used by tests also.
  */
trait Global
    extends GlobalSettings
    with ScaldiSupport {

  def applicationModule = new WebModule :: new UserModule

  val defaultUserEmail = "admin@admin.com"

  val defaultUserId = UserId(defaultUserEmail)

  /**
    *
    */
  override def onStart(app: play.api.Application) {
    super.onStart(app)

    checkEmailConfig(app)

    createDefaultUser
    //createTestUser

    addTestData

    createSqlDdlScripts

    Logger.debug(s"Play started")
  }

  override def onStop(app: play.api.Application) {
    super.onStop(app)
    Logger.debug(s"Play stopped")
  }

  def checkEmailConfig(app: play.api.Application) = {
    app.configuration.getString("smtp.host").getOrElse(
      throw new RuntimeException("smtp server information needs to be set in email.conf"))
  }

  /** Used for debugging only.
    *
    * password is "testuser"
    */
  def createTestUser = {
    val userRepository = inject [UserRepository]

    val email = "test@biosample.ca"
    val validation = RegisteredUser.create(
      UserId(email),
      -1L,
      DateTime.now,
      "testuser",
      email,
      "$2a$10$bkENUsLcxClf9gce/Mnv3OQcLcG6S5jP730MxGWSKNSKUmaJ/gdGq",
      "$2a$10$bkENUsLcxClf9gce/Mnv3O",
      None)

    if (validation.isFailure) {
      validation.swap.map { err =>
        throw new RuntimeException("could not add default user in development mode: " + err)
      }
    }
    validation map { user =>
      userRepository.put(user)
    }
  }

  /**
    * for debug only - password is "testuser"
    */
  def createDefaultUser: User = {
    val userRepository = inject [UserRepository]

    Logger.debug("createDefaultUser")
    //if ((app.mode == Mode.Dev) || (app.mode == Mode.Test)) {

    //
    val validation = RegisteredUser.create(
      UserId(defaultUserEmail),
      -1L,
      DateTime.now,
      "admin",
      defaultUserEmail,
      "$2a$10$Kvl/h8KVhreNDiiOd0XiB.0nut7rysaLcKpbalteFuDN8uIwaojCa",
      "$2a$10$Kvl/h8KVhreNDiiOd0XiB.",
      None)

    validation.fold(
      err => throw new RuntimeException("could not add default user in development mode: " + err),
      user => {
        user.activate.fold(
          err => throw new RuntimeException("could not activate default user in development mode: " + err),
          activeUser => {
            Logger.debug("default user created")
            userRepository.put(activeUser)
          }
        )
      }
    )
  }

  def addTestData(): Unit = {
    Logger.debug("addTestData")

    addMultipleStudies
  }

  def addMultipleStudies(): Unit = {
    Logger.debug("addMultipleStudies")
  }

  /**
    * Creates SQL DDL scripts on application start-up.
    */
  private def createSqlDdlScripts(): Unit = {
    // if (app.mode != Mode.Prod) {
    //   app.configuration.getConfig(configKey).foreach { configuration =>
    //     configuration.keys.foreach { database =>
    //       val databaseConfiguration = configuration.getString(database).getOrElse {
    //         throw configuration.reportError(database, "No config: key " + database, None)
    //       }
    //       val packageNames = databaseConfiguration.spl"," in new WithApplication(fakeApplication()).toSet
    //       val classloader = app.classloader
    //       val ddls = TableScanner.reflectAllDDLMethods(packageNames, classloader)

    //       val scriptDirectory = app.getFile(ScriptDirectory + database)
    //       Files.createDirectory(scriptDirectory)

    //       writeScript(ddls.map(_.createStatements), scriptDirectory, CreateScript)
    //       writeScript(ddls.map(_.dropStatements), scriptDirectory, DropScript)
    //     }
    //   }
    // }
  }

  /**
    * Writes the given DDL statements to a file.
    */
  private def writeScript(
    ddlStatements: Seq[Iterator[String]],
    directory: File,
    fileName: String): Unit = {
    // val createScript = new File(directory, fileName)
    // val createSql = ddlStatements.flatten.mkString("\n\n")
    // Files.writeFileIfChanged(createScript, ScriptHeader + createSql)
  }

}


object Global
    extends WithFilters(new GzipFilter(shouldGzip = (request, response) => {
      val contentType = response.headers.get("Content-Type")
      contentType.exists(_.startsWith("text/html")) || request.path.endsWith("jsroutes.js")
    }))
    with Global

