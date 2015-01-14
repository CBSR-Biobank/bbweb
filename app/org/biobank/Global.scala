package org.biobank

import org.biobank.domain.user.{
  RegisteredUser,
  User,
  UserId,
  UserRepository
}
import org.biobank.domain.study.StudyRepository
import org.biobank.domain.study._
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
    val studyRepository = inject [StudyRepository]

    Logger.debug("addMultipleStudies")

    val studyData = List(
      ("SPARK", "A phase II randomized blinded controlled trial of the effect of furoSemide in cricially ill Patients with eARly acute Kidney injury"),
      ("AHFEM", "Acute Heart Failure-Emergency Management"),
      ("BBPSP", "Blood Borne Pathogens Surveillance Project"),
      ("CHILD", "Canadian Health Infant Longitudinal Development Study"),
      ("CEGIIR", "Centre of Excellence for Gastrointestinal Inflammation and Immunity Research"),
      ("CCCS", "Critical Care Cohort Study"),
      ("ERCIN", "Exploring the Renoprotective effects of fluid prophylaxis strategies for Contrast Induced Nephropathy (Study)"),
      ("FIDS", "Fedorak Iron Deficiency Study"),
      ("HEART", "Heart failure Etiology and Analysis Research Team"),
      ("KDCS", "Kidney Disease Cohort Study"),
      ("KMS", "Kingston Merger Study"),
      ("LCS", "Laboratory Controls Study"),
      ("MPS", "Man-Chui Poon Study"),
      ("NHS", "Novartis Hepatitis C Study"),
      ("RVS", "Retroviral Study"),
      ("TCKS", "Tonelli Chronic Kidney Study"),
      ("VAS", "Vascular Access Study"),
      ("FABRY", "Enzyme replacement therapy in patients with Fabry disease: differential impact on Heart Remodeling and Vascular Function"),
      ("CDGI", "Crohn's Disease Genetic Inflizimab"),
      ("CRM", "Diagnostic Marker for a Colorectal Cancer Blood Test"),
      ("PSS", "(Dr.) Parent Scoliosis Study"),
      ("REFINE", "REFINE ICD"),
      ("CaRE", "CaRE"),
      ("Novel - ESRD", "Novel - ESRD"),
      ("KIDNI", "KIDNI"),
      ("PG1", "Phenomic Gap"),
      ("Spinal Stiffness", "Spinal Stiffness"),
      ("QPCS", "Quebec Pancreas Cancer Study"),
      ("FALLOT", "FALLOT"),
      ("HAART", "Randomized Controlled Pilot Study of Highly Active Anti-Retroviral Therapy"),
      ("AKI", "Acute Kidney Injury"),
      ("DG Study", "Delta Genomics Study"),
      ("DBStudy", "DBTestStudy"),
      ("CCSC Demo", "CCSC Demo"),
      ("ZEST", "ZEST"),
      ("Asthma", "Asthma"),
      ("CSF", "CSF"),
      ("REIM", "Resilience Enhancement in Military Populations Through Multiple Health Status Assessments"),
      ("DDPS", "Double Dialysate Phosphate Study"),
      ("PROBE", "PROBE"),
      ("CITP", "Clinical Islet Transplant Program"),
      ("Caspase", "CITP Caspase"),
      ("iGenoMed", "iGenoMed"),
      ("JB", "Bradwein"),
      ("NEC", "Necrotizing Enterocolitis Study"),
      ("TMIC", "TMIC")
    )

    val studies = studyData.map { case (name, description) =>
      val study: Study = DisabledStudy(
        id           = studyRepository.nextIdentity,
        version      = 0L,
        timeAdded    = DateTime.now,
        timeModified = None,
        name         = name,
        description  = Some(description)
      )
      studyRepository.put(study)
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

