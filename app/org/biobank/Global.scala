package org.biobank

import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.user._

import play.api.GlobalSettings
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter
import org.biobank.modules.{WebModule, UserModule}
import play.api.libs.concurrent.Akka
import play.api.Logger
import play.api.Mode

import akka.actor.ActorSystem
import akka.actor.Props

import java.io.File
import org.joda.time.DateTime
import scaldi.play.ScaldiSupport
import scaldi.Module
import scaldi.akka.AkkaInjectable

/**
 * This is a trait so that it can be used by tests also.
 */
trait Global
    extends GlobalSettings
    with ScaldiSupport {

  def applicationModule = new WebModule :: new UserModule

  /**
   *
   */
  override def onStart(app: play.api.Application) {
    super.onStart(app)

    applicationModule
    checkEmailConfig(app)
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
    extends WithFilters(
  new GzipFilter(shouldGzip = (request, response) => {
                   val contentType = response.headers.get("Content-Type")
                   contentType.exists(_.startsWith("text/html")) || request.path.endsWith("jsroutes.js")
                 }))
    with Global {

    val DefaultUserEmail = "admin@admin.com"

    val DefaultUserId = UserId(DefaultUserEmail)

}

