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
import play.api.Mode

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

    if (app.mode == Mode.Dev) {
      addTestData
    }

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
      ("4e93fa34e51f4f638888989f28905693", "AHFEM", "Acute Heart Failure-Emergency Management"),
      ("1fb4ba368b45459c90ad9fb05a6ec0ae", "AKI", "Acute Kidney Injury"),
      ("ccf1861f6b3246c0baef53583dc40254", "Asthma", "Asthma"),
      ("603825c107154f3eadb51a47a1e7bdb9", "BBPSP", "Blood Borne Pathogens Surveillance Project"),
      ("3e58b55f11d54deeac53195e259ba90a", "CCCS", "Critical Care Cohort Study"),
      ("269df1f301084c6ab65e4c79a0a5d4af", "CCSC Demo", "CCSC Demo"),
      ("dad9df4d87814e4c8ccde08fde6e9645", "CDGI", "Crohn's Disease Genetic Inflizimab"),
      ("d187e4d71b6243b3a6edfe1fd7d81a25", "CEGIIR", "Centre of Excellence for Gastrointestinal Inflammation and Immunity Research"),
      ("c2a0c777478d4acaac47466c62182e1b", "CHILD", "Canadian Health Infant Longitudinal Development Study"),
      ("8bf39b70c15b495c9df8da68119f5b71", "CITP", "Clinical Islet Transplant Program"),
      ("dab1b645e8f44be3b173cc264cdb4095", "CRM", "Diagnostic Marker for a Colorectal Cancer Blood Test"),
      ("5a37c3f630a14027929ff1d26d6cff53", "CSF", "CSF"),
      ("db2eb5bc806a4ba9976aa4f801f556ea", "CaRE", "CaRE"),
      ("43e27e59160a4fef9178f2d7a1b41e81", "Caspase", "CITP Caspase"),
      ("ad2d2b5c5d0248439b55aac597ba94aa", "DBStudy", "DBTestStudy"),
      ("81f25e477bc545b7849c61f846b63f2b", "DDPS", "Double Dialysate Phosphate Study"),
      ("ab4d147597994fd58a00ab0eda1ffc9a", "DG Study", "Delta Genomics Study"),
      ("e45faebf04a245d1aec16606d065f3ff", "ERCIN", "Exploring the Renoprotective effects of fluid prophylaxis strategies for Contrast Induced Nephropathy (Study)"),
      ("61a9979d03344184b3d07f10a0b9e13b", "FABRY", "Enzyme replacement therapy in patients with Fabry disease: differential impact on Heart Remodeling and Vascular Function"),
      ("24688c2884864e47a5e37d593c95f7c0", "FALLOT", "FALLOT"),
      ("3acecdf51458473393391e7ed41139cf", "FIDS", "Fedorak Iron Deficiency Study"),
      ("c11f0827ada84e1ebb536e0c0bead1b3", "HAART", "Randomized Controlled Pilot Study of Highly Active Anti-Retroviral Therapy"),
      ("0462550c728442278852953e646443ac", "HEART", "Heart failure Etiology and Analysis Research Team"),
      ("b5c99ddcc0894ff49fbb85e77cd972f9", "JB", "Bradwein"),
      ("b457ea0dfa674fd1ade86f17fce86363", "KDCS", "Kidney Disease Cohort Study"),
      ("ca279411b5c0463786afeb7d6ce9edff", "KIDNI", "KIDNI"),
      ("4c3a7438edcf440aa92f3c6501ae183c", "KMS", "Kingston Merger Study"),
      ("256856f356de4956ab3fed7b029cb014", "LCS", "Laboratory Controls Study"),
      ("b610fdb1afce462584ad0570cce3c350", "MPS", "Man-Chui Poon Study"),
      ("71d59e03663847d6b630b7912fd4c5ae", "NEC", "Necrotizing Enterocolitis Study"),
      ("f5dcd46033754f2bb830607899bbfc92", "NHS", "Novartis Hepatitis C Study"),
      ("e3d28a6c6e5e409a81b4581b9be78309", "Novel - ESRD", "Novel - ESRD"),
      ("5742d95d01ce47479984170662366cac", "PG1", "Phenomic Gap"),
      ("bc59d71985a54294b6c254d7b7cc41e6", "PROBE", "PROBE"),
      ("f0146da9ac39424791b3310345ceef92", "PSS", "(Dr.) Parent Scoliosis Study"),
      ("fbd4a19d5c2f458196551d4148150980", "QPCS", "Quebec Pancreas Cancer Study"),
      ("526146910a1248e59a2791a1771359c6", "REFINE", "REFINE ICD"),
      ("01954dc6455342f2beedb37b2222cb59", "REIM", "Resilience Enhancement in Military Populations Through Multiple Health Status Assessments"),
      ("23995f5fd6a54817ad117c46e4ce7b7a", "RVS", "Retroviral Study"),
      ("23bfe763977f4055ae4561163cb5268f", "SPARK", "A phase II randomized blinded controlled trial of the effect of furoSemide in cricially ill Patients with eARly acute Kidney injury"),
      ("e10c61b13bb4448db011a26d8297eb8c", "Spinal Stiffness", "Spinal Stiffness"),
      ("6b4f09d4b7b340aa9387ca200351d638", "TCKS", "Tonelli Chronic Kidney Study"),
      ("ead7f198faea466dbb5d3198a19d9d5f", "TMIC", "TMIC"),
      ("a0e87a38718b4e18a871271f921e9612", "VAS", "Vascular Access Study"),
      ("07f1a4f8a46b463884432f65dc732f60", "ZEST", "ZEST"),
      ("0386e23157394b13b5c53993f0ad0a56", "iGenoMed", "iGenoMed")
    )

    val studies = studyData.map { case (id, name, description) =>
      val study: Study = DisabledStudy(
        id           = StudyId(id),
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

