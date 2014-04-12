package controllers

import play.api.mvc.Results._
import play.api.mvc.RequestHeader
import java.io.File
import play.api.db.slick.plugin.TableScanner
import play.api.libs.Files
import play.api.{ Configuration, GlobalSettings, Logger, Mode }
import play.api.Play.current
import play.api.db.slick._
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.session.Database
import scala.slick.jdbc.meta._

/**
 * Global settings for the web application.
 *
 * On application start, [[onStart]], the Eventsourced application is started.
 *
 * If the application is running in '''development''' mode, the query side DDL database scritps are
 * also generated.
 */
object WebComponent extends GlobalSettings with org.biobank.service.TopComponentImpl {

  private val configKey = "slick"
  private val ScriptDirectory = "conf/evolutions/"
  private val CreateScript = "create-database.sql"
  private val DropScript = "drop-database.sql"
  private val ScriptHeader = "-- SQL DDL script\n-- Generated file - do not edit\n\n"

  /**
   * On application startup, also start the Eventsourced framework.
   *
   * It is important to do it at this stage of initialization since the query side database session
   * must already be initialized before Eventsourced starts.
   */
  override def onStart(app: play.api.Application) {
    createSqlDdlScripts(app)
  }

  /**
   * Creates SQL DDL scripts on application start-up.
   */
  private def createSqlDdlScripts(app: play.api.Application) {
    if (app.mode != Mode.Prod) {
      app.configuration.getConfig(configKey).foreach { configuration =>
        configuration.keys.foreach { database =>
          val databaseConfiguration = configuration.getString(database).getOrElse {
            throw configuration.reportError(database, "No config: key " + database, None)
          }
          val packageNames = databaseConfiguration.split(",").toSet
          val classloader = app.classloader
          val ddls = TableScanner.reflectAllDDLMethods(packageNames, classloader)

          val scriptDirectory = app.getFile(ScriptDirectory + database)
          Files.createDirectory(scriptDirectory)

          writeScript(ddls.map(_.createStatements), scriptDirectory, CreateScript)
          writeScript(ddls.map(_.dropStatements), scriptDirectory, DropScript)
        }
      }
    }
    Logger.info("*** application started ***")
  }

  /**
   * Writes the given DDL statements to a file.
   */
  private def writeScript(ddlStatements: Seq[Iterator[String]], directory: File,
    fileName: String): Unit = {
    val createScript = new File(directory, fileName)
    val createSql = ddlStatements.flatten.mkString("\n\n")
    Files.writeFileIfChanged(createScript, ScriptHeader + createSql)
  }
}
