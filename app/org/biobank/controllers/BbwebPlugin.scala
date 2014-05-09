package org.biobank.controllers

import org.biobank.domain.{ RegisteredUser, UserId }
import org.biobank.service.TopComponentImpl

import java.io.File
import play.api.libs.Files
import play.api.{ Configuration, GlobalSettings, Logger, Mode, Plugin }
import play.libs.Akka
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory

class BbwebPlugin(val app: play.api.Application) extends Plugin with TopComponentImpl {
  implicit override val system: akka.actor.ActorSystem = Akka.system

  override val studyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")
  override val userProcessor = system.actorOf(Props(new UserProcessor), "userproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
  override val userService = new UserService(userProcessor)

  private val configKey = "slick"
  private val ScriptDirectory = "conf/evolutions/"
  private val CreateScript = "create-database.sql"
  private val DropScript = "drop-database.sql"
  private val ScriptHeader = "-- SQL DDL script\n-- Generated file - do not edit\n\n"

  /**
   *
   */
  override def onStart() {
    createSqlDdlScripts

    if (app.mode == Mode.Dev) {

      if (userRepository.isEmpty) {
        // for debug only - password is "administrator"
        val email = "admin@admin.com"
        val validation = RegisteredUser.create(
          UserId(email), -1L, "admin", email,
          "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS", "bcrypt", None, None)
        if (validation.isFailure) {
          throw new Error("could not add default user in development mode")
        }
        validation map { user =>
          userRepository.put(user)
        }
      }
    }

    super.onStart
  }

  /**
   * Creates SQL DDL scripts on application start-up.
   */
  private def createSqlDdlScripts: Unit = {
    // if (app.mode != Mode.Prod) {
    //   app.configuration.getConfig(configKey).foreach { configuration =>
    //     configuration.keys.foreach { database =>
    //       val databaseConfiguration = configuration.getString(database).getOrElse {
    //         throw configuration.reportError(database, "No config: key " + database, None)
    //       }
    //       val packageNames = databaseConfiguration.split(",").toSet
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
