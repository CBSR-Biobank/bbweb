package org.biobank

import akka.actor.ActorSystem
import javax.inject.{ Inject, Singleton }
import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service.PasswordHasher
import org.hashids.Hashids
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import play.api.Logger
import scalaz.Scalaz._

/**
 * Provides initial data to test with.
 */
object TestData {

  val configPath = "application.loadTestData"

  val centreData = List(
      ("CL1-Foothills", "CL1-Foothills"),
      ("CL1-Heritage", "CL1-Heritage"),
      ("CL1-Sunridge", "CL1-Sunridge"),
      ("CL2-Children Hosp", "CL2-Alberta's Children's Hospital"),
      ("ED1-UofA", "ED1-UofA"),
      ("OT2-Children Hosp", "OT2-Children's Hospital of Eastern Ontario"),
      ("QB1-Enfant-Jesus", "QB1-Hopital Enfant-Jesus"),
      ("RD1-Red Deer Hosp", "RD1-Red Deer Regional Hospital"),
      ("SB1-St John NB Hosp", "SB1-Saint Johns NB Regional Hospital"),
      ("SD1-Sudbury Hosp", "SD1-Sudbury Regional Hospital"),
      ("SF1-Health NFLD", "SF1-Health Science Center"),
      ("SP1-St Therese Hosp", "SP1-St Therese Hospital"),
      ("SS1-Royal Hosp", "SS1-Royal University Hospital"),
      ("TH1-Regional Hosp", "TH1-Thunder Bay Regional Hospital"),
      ("TR1-St Mikes", "TR1-St Michael's Hospital"),
      ("VN1-St Paul", "VN1-St Paul's Hospital"),
      ("VN2-Childrens Hosp", "VN2-BC Women and Children's Hospital"),
      ("WL1-Westlock Hosp", "WL1-Westlock Health Care Center"),
      ("WN1-Cancer Care", "WN1-Cancer Care Manitoba"),
      ("CL1-Foothills TRW", "CL1-Foothills TRW"),
      ("CBSR", "Canadian BioSample Repository"),
      ("Calgary-F", "Calgary Foothills"),
      ("100-Calgary AB", "100-Calgary Alberta Refine"),
      ("College Plaza UofA", "College Plaza UofA"),
      ("101-London ON", "101-London Ontario Refine"),
      ("102-Newmarket ON", "102-Newmarket Ontario Refine"),
      ("103-Montreal QC", "103-Montreal Quebec Refine"),
      ("104-Montreal QC", "104-Montreal Quebec Refine"),
      ("105-Victoria BC", "105-Victoria British Columbia refine"),
      ("106-Quebec City QC", "106-Quebec City Quebec Refine"),
      ("CaRE ED1", "CaRE ED1"),
      ("200-Spokane WA", "200-Spokane Washington REFINE"),
      ("201-Erie PA", "201-Erie Pennsylvania REFINE"),
      ("202-Cherry Hill NJ", "202-Cherry Hill New Jersey REFINE"),
      ("203-West Des Moines IA", "203-West Des Moines Iowa REFINE"),
      ("204-Evansville IN", "204-Evansville Indiana REFINE"),
      ("205-Southfield MI", "205-Southfield Michigan REFINE"),
      ("206 A -Nashville TN", "206 A -Nashville Tennessee REFINE"),
      ("207-Amarillo TX", "207-Amarillo Texas REFINE"),
      ("300-Oulu FI", "300-Oulu Finland REFINE"),
      ("ED1-GNH", "ED1-GNH"),
      ("KIDNI CL1", "KIDNI Calgary")
    )

  val studyData = List(
      ("AHFEM", "Acute Heart Failure-Emergency Management"),
      ("AKI", "Acute Kidney Injury"),
      ("Asthma", "Asthma"),
      ("BBPSP", "Blood Borne Pathogens Surveillance Project"),
      ("CCCS", "Critical Care Cohort Study"),
      ("CCSC Demo", "CCSC Demo"),
      ("CDGI", "Crohn's Disease Genetic Inflizimab"),
      ("CEGIIR", "Centre of Excellence for Gastrointestinal Inflammation and Immunity Research"),
      ("CHILD", "Canadian Health Infant Longitudinal Development Study"),
      ("CITP", "Clinical Islet Transplant Program"),
      ("CRM", "Diagnostic Marker for a Colorectal Cancer Blood Test"),
      ("CSF", "CSF"),
      ("CaRE", "CaRE"),
      ("Caspase", "CITP Caspase"),
      ("DBStudy", "DBTestStudy"),
      ("DDPS", "Double Dialysate Phosphate Study"),
      ("DG Study", "Delta Genomics Study"),
      ("ERCIN", "Exploring the Renoprotective effects of fluid prophylaxis strategies for Contrast Induced Nephropathy (Study)"),
      ("FABRY", "Enzyme replacement therapy in patients with Fabry disease: differential impact on Heart Remodeling and Vascular Function"),
      ("FALLOT", "FALLOT"),
      ("FIDS", "Fedorak Iron Deficiency Study"),
      ("HAART", "Randomized Controlled Pilot Study of Highly Active Anti-Retroviral Therapy"),
      ("HEART", "Heart failure Etiology and Analysis Research Team"),
      ("JB", "Bradwein"),
      ("KDCS", "Kidney Disease Cohort Study"),
      ("KIDNI", "KIDNI"),
      ("KMS", "Kingston Merger Study"),
      ("LCS", "Laboratory Controls Study"),
      ("MPS", "Man-Chui Poon Study"),
      ("NEC", "Necrotizing Enterocolitis Study"),
      ("NHS", "Novartis Hepatitis C Study"),
      ("Novel - ESRD", "Novel - ESRD"),
      ("PG1", "Phenomic Gap"),
      ("PROBE", "PROBE"),
      ("PSS", "(Dr.) Parent Scoliosis Study"),
      ("QPCS", "Quebec Pancreas Cancer Study"),
      ("REFINE", "REFINE ICD"),
      ("REIM", "Resilience Enhancement in Military Populations Through Multiple Health Status Assessments"),
      ("RVS", "Retroviral Study"),
      ("SPARK", "A phase II randomized blinded controlled trial of the effect of furoSemide in cricially ill Patients with eARly acute Kidney injury"),
      ("Spinal Stiffness", "Spinal Stiffness"),
      ("TCKS", "Tonelli Chronic Kidney Study"),
      ("TMIC", "TMIC"),
      ("VAS", "Vascular Access Study"),
      ("ZEST", "ZEST"),
      ("iGenoMed", "iGenoMed")
    )

  val userData = List(
      ("Nelson Loyola", "loyola@ualberta.ca"),
      ("Luisa Franco", "lfrancor@ucalgary.ca"),
      ("Corazon Oballo", "coballo@ucalgary.ca"),
      ("Amie Lee", "amie1@ualberta.ca"),
      ("Lisa Tanguay", "lisa.tanguay@ualberta.ca"),
      ("Darlene Ramadan", "ramadan@ucalgary.ca"),
      ("Juline Skripitsky", "Jskrip@biosample.ca"),
      ("Leslie Jackson Carter", "jacksola@ucalgary.ca"),
      ("Thiago Oliveira", "toliveir@ucalgary.ca"),
      ("Rozsa Sass", "rsas@ucalgary.ca"),
      ("Margaret Morck", "mmorck@ucalgary.ca"),
      ("Kristan Nagy", "nagy1@ualberta.ca"),
      ("Bruce Ritchie", "bruce.ritchie@ualberta.ca"),
      ("Matthew Klassen", "mwklasse@ualberta.ca"),
      ("Marleen Irwin", "mirwin@ualberta.ca"),
      ("Millie Silverstone", "millie.silverstone@me.com"),
      ("Trevor Soll", "tsoll@ualberta.ca"),
      ("Stephanie Wichuk", "stephaniewichuk@med.ualberta.ca"),
      ("Deborah Parfett", "dparfett@catrials.org"),
      ("Samantha Taylor", "samantha.taylor@albertahealthservices.ca"),
      ("Martine Bergeron", "martine.bergeron@crchum.qc.ca"),
      ("Isabelle Deneufbourg", "isabelle.deneufbourg@criucpq.ulaval.ca"),
      ("Colin Coros", "coros@ualberta.ca"),
      ("Ray Vis", "rvis@ualberta.ca"),
      ("Suzanne Morissette", "suzanne.morissette.chum@ssss.gouv.qc.ca"),
      ("Francine Marsan", "francine.marsan.chum@ssss.gouv.qc.ca"),
      ("Jeanne Bjergo", "jeannebjergo@hcnw.com"),
      ("Larissa Weeks", "larissaweeks@hcnw.com"),
      ("Sharon Fulton", "sharonfulton@hcnw.com"),
      ("Mirjana Maric Viskovic", "maric@ucalgary.ca"),
      ("Paivi Kastell", "paivi.kastell@ppshp.fi"),
      ("Paivi Koski", "paivi.koski@ppshp.fi")
    )

  val ahfemDescription =
      s"""|Magnis turpis mollis. Duis commodo libero. Turpis magnis massa morbi cras non mollis, maecenas
          |dictumst venenatis augue, rhoncus id non eros nec odio. Ut wisi ullamcorper elit parturient,
          |venenatis libero et, pellentesque sed, purus erat nonummy diam. Hendrerit porro lobortis.
          |
          |Eget proin ligula blandit ante magna aenean. Purus et maecenas, venenatis nonummy dolor quam
          |dictumst. Auctor etiam, ligula eu, senectus iaculis ante. Sed urna, viverra pellentesque
          |scelerisque libero vel, vitae neque nascetur nibh turpis, ridiculus pede maecenas rutrum per
          |cubilia ultrices. Lacus sapien odio per ac nulla lectus. Morbi vitae a laoreet vehicula lectus,
          |rutrum convallis diam, arcu ipsum egestas facilis eleifend, tellus neque rutrum ut wisi in. Sit
          |velit sociis placerat neque id, imperdiet ut a urna ac, sed accumsan, fusce nunc dolor, et donec
          |orci quis. Magnis vestibulum dapibus leo consectetuer blandit, ac eget, porta tempor semper urna
          |tempor diam.
          |""".stripMargin

}

/**
 * Provides initial data to test with. Ideally these methods should only be called for developemnt builds.
 */
@Singleton
class TestData @Inject() (val actorSystem:                   ActorSystem,
                          val passwordHasher:                PasswordHasher,
                          val collectionEventTypeRepository: CollectionEventTypeRepository,
                          val processingTypeRepository:      ProcessingTypeRepository,
                          val specimenGroupRepository:       SpecimenGroupRepository,
                          val specimenLinkTypeRepository:    SpecimenLinkTypeRepository,
                          val studyRepository:               StudyRepository,
                          val participantRepository:         ParticipantRepository,
                          val userRepository:                UserRepository,
                          val centreRepository:              CentreRepository) {
  import TestData._

  val log = LoggerFactory.getLogger(this.getClass)

  private val loadTestData = (actorSystem.settings.config.hasPath(configPath)
                                && actorSystem.settings.config.getBoolean(configPath))

  def addMultipleCentres(): Unit = {
    if (loadTestData) {
      Logger.debug("addMultipleCentres")

      val hashids = Hashids("test-data-centres")

      centreData.zipWithIndex.foreach { case ((name, description), index) =>
        val centre: Centre = DisabledCentre(
            id           = CentreId(hashids.encode(index)),
            version      = 0L,
            timeAdded    = DateTime.now,
            timeModified = None,
            name         = name,
            description  = Some(description),
            studyIds     = Set.empty,
            locations    = Set.empty)
        centreRepository.put(centre)
      }
    }
    ()
  }

  def addMultipleStudies(): Unit = {
    if (loadTestData) {
      Logger.debug("addMultipleStudies")

      val hashids = Hashids("test-data-studies")

      studyData.zipWithIndex.foreach { case ((name, description), index) =>
        val descMaybe = if (name == "AHFEM") Some(s"$description\n\n$ahfemDescription")
                        else Some(description)

        val study: Study = DisabledStudy(id              = StudyId(hashids.encode(index)),
                                         version         = 0L,
                                         timeAdded       = DateTime.now,
                                         timeModified    = None,
                                         name            = name,
                                         description     = descMaybe,
                                         annotationTypes = getBbpspParticipantAnnotationTypes)
        studyRepository.put(study)
      }

      addCollectionEvents
    }
    ()
  }

  def addCollectionEvents(): Unit = {
    Logger.debug("addCollectionEvents")

    studyRepository.getValues
      .find { s => s.name == "BBPSP"}
      .foreach { bbpsp =>
      val hashids = Hashids("test-data-cevent-types")

      // Use a list since "id" is determined at the time of adding to the repository
      val ceventTypes =
        List(CollectionEventType(studyId            = bbpsp.id,
                                 id                 = CollectionEventTypeId(hashids.encode(1)),
                                 version            = 0L,
                                 timeAdded          = DateTime.now,
                                 timeModified       = None,
                                 name               = "Default Event ",
                                 description        = None,
                                 recurring          = true,
                                 specimenSpecs      = getBbpspSpecimenSpecs,
                                 annotationTypes    = getBbpspCeventAnnotationTypes),
             CollectionEventType(studyId            = bbpsp.id,
                                 id                 = CollectionEventTypeId(hashids.encode(2)),
                                 version            = 0L,
                                 timeAdded          = DateTime.now,
                                 timeModified       = None,
                                 name               = "Second Event ",
                                 description        = Some("Example event"),
                                 recurring          = false,
                                 specimenSpecs      = getBbpspSpecimenSpecs,
                                 annotationTypes    = Set.empty))

      ceventTypes.foreach { cet => collectionEventTypeRepository.put(cet) }
      ()
    }
    ()
  }

  def getBbpspSpecimenSpecs() = {
    val hashids = Hashids("bbpsp-specimen-specs")

    Set(CollectionSpecimenSpec(
          uniqueId                    = hashids.encode(1),
          name                        = "10 mL Lavender top EDTA tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.WholeBloodEdta,
          maxCount                    = 2, // set to 2 for testing form, set back to 1 for demo
          amount                      = Some(10)),
        CollectionSpecimenSpec(
          uniqueId                    = hashids.encode(2),
          name                        = "10 mL Orange top PAXgene tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.Paxgene,
          maxCount                    = 1,
          amount                      = Some(10))
          //,
        // CollectionSpecimenSpec(
        //   uniqueId                    = hashids.encode(3),
        //   name                        = "3mL Lavender top EDTA tube",
        //   description                 = None,
        //   units                       = "mL",
        //   anatomicalSourceType        = AnatomicalSourceType.Blood,
        //   preservationType            = PreservationType.FreshSpecimen,
        //   preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
        //   specimenType                = SpecimenType.WholeBloodEdta,
        //   maxCount                    = 1,
        //   amount                      = Some(3)),
        // CollectionSpecimenSpec(
        //   uniqueId                    = hashids.encode(4),
        //   name                        = "4ml lavender top EDTA tube",
        //   description                 = None,
        //   units                       = "mL",
        //   anatomicalSourceType        = AnatomicalSourceType.Blood,
        //   preservationType            = PreservationType.FreshSpecimen,
        //   preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
        //   specimenType                = SpecimenType.WholeBloodEdta,
        //   maxCount                    = 1,
        //   amount                      = Some(4)),
        // CollectionSpecimenSpec(
        //   uniqueId                    = hashids.encode(5),
        //   name                        = "9ml CPDA yellow top tube",
        //   description                 = None,
        //   units                       = "mL",
        //   anatomicalSourceType        = AnatomicalSourceType.Blood,
        //   preservationType            = PreservationType.FreshSpecimen,
        //   preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
        //   specimenType                = SpecimenType.WholeBloodEdta,
        //   maxCount                    = 1,
        //   amount                      = Some(9)),
        // CollectionSpecimenSpec(
        //   uniqueId                    = hashids.encode(6),
        //   name                        = "Urine cup",
        //   description                 = None,
        //   units                       = "mL",
        //   anatomicalSourceType        = AnatomicalSourceType.Urine,
        //   preservationType            = PreservationType.FreshSpecimen,
        //   preservationTemperatureType = PreservationTemperatureType.Plus4celcius,
        //   specimenType                = SpecimenType.CdpaPlasma,
        //   maxCount                    = 1,
        // amount                      = Some(15)
        //)
    )
  }

  def getBbpspParticipantAnnotationTypes() = {
    val hashids = Hashids("bbpsp-participant-annotation-types")

    Set(AnnotationType(
          uniqueId      = hashids.encode(1),
          name          = "Date of birth",
          description   = None,
          valueType     = AnnotationValueType.DateTime,
          maxValueCount = None,
          options       = Seq.empty[String],
          required      = true),
        AnnotationType(
          uniqueId      = hashids.encode(2),
          name          = "Gender",
          description   = None,
          valueType     = AnnotationValueType.Select,
          maxValueCount = Some(1),
          options       = Seq("Female", "Male"),
          required      = true))
  }

  def getBbpspCeventAnnotationTypes() = {
    val hashids = Hashids("bbpsp-collection-event-annotation-types")

    Set(AnnotationType(
          uniqueId      = hashids.encode(1),
          name          = "Phlebotomist",
          description   = None,
          valueType     = AnnotationValueType.Text,
          maxValueCount = None,
          options       = Seq.empty[String],
          required      = true),
        AnnotationType(
          uniqueId      = hashids.encode(2),
          name          = "Consent",
          description   = None,
          valueType     = AnnotationValueType.Select,
          maxValueCount = Some(2),
          options       = Seq("Surveillance", "Genetic Predisposition", "Previous Samples", "Genetic Mutation"),
          required      = true))
  }

  def addMultipleUsers() = {
    if (loadTestData) {
      Logger.debug("addMultipleUsers")

      val plainPassword = "testuser"
      val salt = passwordHasher.generateSalt
      val hashids = Hashids("test-data-users")

      userData.zipWithIndex.foreach { case((name, email), index) =>
        val user: User = ActiveUser(
            id           = UserId(hashids.encode(index)),
            version      = 0L,
            timeAdded    = DateTime.now,
            timeModified = None,
            name         = name,
            email        = email,
            password     = passwordHasher.encrypt(plainPassword, salt),
            salt         = salt,
            avatarUrl    = None
          )
        userRepository.put(user)
      }
    }
  }

}
