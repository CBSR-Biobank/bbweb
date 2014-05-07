// Upgraded to Play 2.3-M1 and it does not yet have a Slick plugin.
//
// Commenting out code for now
// import play.api.db.slick.plugin.TableScanner
// import play.api.db.slick._
// import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
// import scala.slick.session.Database
// import scala.slick.jdbc.meta._

import org.biobank.controllers.ApplicationComponent
import org.biobank.domain.{ RegisteredUser, UserId }

//import play.api.mvc.Results._
//import play.api.mvc.RequestHeader
import java.io.File
import play.api.libs.Files
import play.api.{ Configuration, GlobalSettings, Logger, Mode }
import org.slf4j.LoggerFactory

/**
 * Global settings for the web application.
 *
 * If the application is running in '''development''' mode, the query side DDL database scritps are
 * also generated.
 */
object Global extends GlobalSettings {

  val log = LoggerFactory.getLogger(this.getClass)

  private val configKey = "slick"
  private val ScriptDirectory = "conf/evolutions/"
  private val CreateScript = "create-database.sql"
  private val DropScript = "drop-database.sql"
  private val ScriptHeader = "-- SQL DDL script\n-- Generated file - do not edit\n\n"

  /**
   *
   */
  override def onStart(app: play.api.Application) {
    createSqlDdlScripts(app)

    if (app.mode == Mode.Dev) {

      if (ApplicationComponent.userRepository.isEmpty) {
        // for debug only - password is "administrator"
        val email = "admin@admin.com"
        val validation = RegisteredUser.create(
          UserId(email), -1L, "admin", email,
          "$2a$10$ErWon4hGrcvVRPa02YfaoOyqOCxvAfrrObubP7ZycS3eW/jgzOqQS", "bcrypt", None, None)
        if (validation.isFailure) {
          throw new Error("could not add default user in development mode")
        }
        validation map { user =>
          ApplicationComponent.userRepository.put(user)
        }
      }
    }

    super.onStart(app)
  }

  /**
   * Creates SQL DDL scripts on application start-up.
   */
  private def createSqlDdlScripts(app: play.api.Application) {
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
    Logger.info("*** application started ***")
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
