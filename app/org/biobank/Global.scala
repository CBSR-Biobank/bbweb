package org.biobank

import akka.stream.Materializer
import javax.inject._
import org.biobank.domain.user._
import org.joda.time.{DateTime, DateTimeZone}
import play.api.{Configuration}
import play.filters.gzip.GzipFilter

/**
 * This is a trait so that it can be used by tests also.
 */
@Singleton
@SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.ImplicitParameter"))
class Global @Inject()(implicit val mat: Materializer,
                       configuration: Configuration) {

  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  val filter = new GzipFilter(shouldGzip = (request, response) => {
                                  response.body.contentType.exists(_.startsWith("text/html"))
                   })
  checkEmailConfig
  createSqlDdlScripts

  def checkEmailConfig(): Unit = {
    if (configuration.getString("play.mailer.host").isEmpty) {
      throw new RuntimeException("smtp server information needs to be set in email.conf")
    }
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
  // private def writeScript(
  //   ddlStatements: Seq[Iterator[String]],
  //   directory: File,
  //   fileName: String): Unit = {
  //   // val createScript = new File(directory, fileName)
  //   // val createSql = ddlStatements.flatten.mkString("\n\n")
  //   // Files.writeFileIfChanged(createScript, ScriptHeader + createSql)
  // }

}

object Global {

  val DefaultUserEmail: String = "admin@admin.com"

  val DefaultUserId: UserId = UserId(DefaultUserEmail)

  val StartOfTime: DateTime = new DateTime(1, 1, 1, 0, 0, 0, DateTimeZone.UTC)

  val EndOfTime: DateTime = new DateTime(9999, 1, 1, 0, 0, 0, DateTimeZone.UTC)

}
