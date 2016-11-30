package org.biobank

import javax.inject.{ Inject, Singleton }
import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service.PasswordHasher
import org.hashids.Hashids
import org.joda.time.DateTime
import play.api.{ Configuration, Environment, Logger, Mode }
import scalaz.Scalaz._

/**
 * Provides initial data to test with.
 */
object TestData {

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
 * Provides initial data to test with. Ideally these methods should only be called for development builds.
 */
@Singleton
class TestData @Inject() (config:                        Configuration,
                          env:                           Environment,
                          passwordHasher:                PasswordHasher,
                          collectionEventTypeRepository: CollectionEventTypeRepository,
                          processingTypeRepository:      ProcessingTypeRepository,
                          specimenGroupRepository:       SpecimenGroupRepository,
                          specimenLinkTypeRepository:    SpecimenLinkTypeRepository,
                          studyRepository:               StudyRepository,
                          participantRepository:         ParticipantRepository,
                          collectionEventRepository:     CollectionEventRepository,
                          userRepository:                UserRepository,
                          centreRepository:              CentreRepository,
                          specimenRepository:            SpecimenRepository,
                          ceventSpecimenRepository:      CeventSpecimenRepository,
                          shipmentRepository:            ShipmentRepository,
                          shipmentSpecimenRepository:    ShipmentSpecimenRepository) {

  import TestData._

  val log = Logger(this.getClass)

  private val loadTestData =
    (env.mode == Mode.Dev) && config.getBoolean("application.testData.load").getOrElse(false)

  private val loadSpecimenTestData =
    (env.mode == Mode.Dev) && config.getBoolean("application.testData.loadSpecimens").getOrElse(false)

  private val loadShipmentTestData =
    (env.mode == Mode.Dev) && config.getBoolean("application.testData.load").getOrElse(false)

  def addMultipleUsers() = {
    if (loadTestData) {
      log.debug("addMultipleUsers")

      val plainPassword = "testuser"
      val salt = passwordHasher.generateSalt
      val hashids = Hashids("test-data-users")

      userData.zipWithIndex.foreach { case((name, email), index) =>
        val user: User = ActiveUser(
            id           = UserId(hashids.encode(index.toLong)),
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

  def addMultipleCentres(): Unit = {
    if (loadTestData) {
      log.debug("addMultipleCentres")

      val hashids = Hashids("test-data-centres")

      centreData.zipWithIndex.foreach { case ((name, description), index) =>
        val centre: Centre = DisabledCentre(
            id           = CentreId(hashids.encode(index.toLong)),
            version      = 0L,
            timeAdded    = DateTime.now,
            timeModified = None,
            name         = name,
            description  = Some(description),
            studyIds     = Set.empty,
            locations    = Set.empty)
        centreRepository.put(centre)
      }

      // enable and add locations for the following centres
      val v = List("100-Calgary AB", "101-London ON").
        map { name =>
          centreRepository.getValues.find(_.name == name).toSuccessNel(s"centre not found")
        }.
        sequenceU

      v.foreach { centres =>
        centres.foreach { centre =>
          val location = if (centre.name == "100-Calgary AB") {
              Location(uniqueId       = s"${centre.name}:Primary",
                       name           = "Primary",
                       street         = "1403 29 St NW",
                       city           = "Calgary",
                       province       = "Alberta",
                       postalCode     = "T2N 2T9",
                       poBoxNumber    = None,
                       countryIsoCode = "CA")
            } else {
              Location(uniqueId       = s"${centre.name}:Primary",
                       name           = "Primary",
                       street         = "London Health Sciences Center, University Hospital, Rm A3-222B, 339 Windermere Road",
                       city           = "London",
                       province       = "Ontario",
                       postalCode     = "N0L 1W0",
                       poBoxNumber    = None,
                       countryIsoCode = "CA")
            }
          centre match {
            case c: DisabledCentre =>
              c.copy(locations = Set(location)).enable.foreach(centreRepository.put)
            case _ =>
          }
        }
      }

      if (v.isFailure) {
        log.error("could not set centre locations")
      }
    }
  }

  def addMultipleStudies(): Unit = {
    if (loadTestData) {
      log.debug("addMultipleStudies")

      val hashids = Hashids("test-data-studies")

      studyData.zipWithIndex.foreach { case ((name, description), index) =>
        val descMaybe = if (name == "AHFEM") Some(s"$description\n\n$ahfemDescription")
                        else Some(description)

        val study: Study = DisabledStudy(id              = StudyId(hashids.encode(index.toLong)),
                                         version         = 0L,
                                         timeAdded       = DateTime.now,
                                         timeModified    = None,
                                         name            = name,
                                         description     = descMaybe,
                                         annotationTypes = getBbpspParticipantAnnotationTypes)
        studyRepository.put(study)
      }

      // add study to centres
      studyRepository.getValues.find(s => s.name == "BBPSP").foreach { bbpsp =>
        List("100-Calgary AB", "101-London ON").
          map { name =>
            centreRepository.getValues.find(_.name == name).toSuccessNel(s"centre not found")
          }.
          sequenceU.
          map { centres =>
            centres.foreach { centre =>
              centre match {
                case c: DisabledCentre => centreRepository.put(c.copy(studyIds = Set(bbpsp.id)))
                case _ =>
              }
            }
          }
      }

      if (loadSpecimenTestData) {
        studyRepository.getValues.find { s => s.name == "BBPSP"}.foreach { bbpsp =>
          bbpsp match {
            case s: DisabledStudy => s.enable.foreach(studyRepository.put)
            case s =>
          }
        }
      }
    }
  }

  def addCollectionEventTypes(): Unit = {
    val hashids = Hashids("test-data-cevent-types")

    log.debug("addCollectionEventTypes")

    studyRepository.getValues
      .find { s => s.name == "BBPSP"}
      .foreach { bbpsp =>

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
    }
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

  def addBbpspParticipants() = {
    if (loadSpecimenTestData) {
      log.debug("addBbpspParticipants")

      val hashids = Hashids("bbpsp-participants")

      studyRepository.getValues
        .find { s => s.name == "BBPSP"}
        .foreach { bbpsp =>

        (0 to 3).foreach { index =>
          participantRepository.put(
            Participant(id           = ParticipantId(hashids.encode(index.toLong)),
                        studyId      = bbpsp.id,
                        version      = 0L,
                        timeAdded    = DateTime.now,
                        timeModified = None,
                        uniqueId     = f"P$index%05d",
                        annotations  = Set.empty[Annotation]))
        }
      }
    }
  }

  def addBbpspCevents() = {
    if (loadSpecimenTestData) {
      val hashids = Hashids("bbpsp-collection-events")

      log.debug(s"addBbpspCevents")

      studyRepository.getValues
        .find { s => s.name == "BBPSP"}
        .foreach { bbpsp =>
        participantRepository.allForStudy(bbpsp.id).zipWithIndex.foreach {
          case (participant, pIndex) =>
            collectionEventTypeRepository.allForStudy(bbpsp.id).zipWithIndex.foreach {
              case (ceventType, cetIndex) =>
                log.debug(s"addBbpspCevents: adding collection event for participant ${participant.uniqueId}")

                val id = CollectionEventId(hashids.encode(10L * pIndex.toLong + cetIndex.toLong))
                collectionEventRepository.put(
                  CollectionEvent(id                    = id,
                                  participantId         = participant.id,
                                  collectionEventTypeId = ceventType.id,
                                  version               = 0L,
                                  timeAdded             = DateTime.now,
                                  timeModified          = None,
                                  timeCompleted         = DateTime.now.minusDays(1),
                                  visitNumber           = cetIndex + 1,
                                  annotations           = Set.empty[Annotation]))
            }
        }
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.TraversableOps"))
  def addBbpspSpecimens() = {

    def addSpecimen(id:           SpecimenId,
                    inventoryId:  String,
                    specimenSpec: CollectionSpecimenSpec,
                    location:     Location) =
      UsableSpecimen(id               = id,
                     inventoryId      = inventoryId,
                     specimenSpecId   = specimenSpec.uniqueId,
                     version          = 0L,
                     timeAdded        = DateTime.now,
                     timeModified     = None,
                     originLocationId = location.uniqueId,
                     locationId       = location.uniqueId,
                     containerId      = None,
                     positionId       = None,
                     timeCreated      = DateTime.now.minusDays(1),
                     amount           = BigDecimal(0.1))


    if (loadSpecimenTestData) {
      log.debug(s"addBbpspSpecimens")

      studyRepository.getValues.find(s => s.name == "BBPSP").foreach { bbpsp =>

        val centreNames = List("100-Calgary AB", "101-London ON")

        centreNames.zipWithIndex.foreach { case (centreName, centreIndex) =>
          centreRepository.getValues.find(_.name == centreName).foreach { centre =>
            centre match {
              case c: EnabledCentre => {
                centreRepository.put(c.copy(studyIds = Set(bbpsp.id)))

                val hashids = Hashids("bbpsp-specimens")
                participantRepository.allForStudy(bbpsp.id).zipWithIndex.
                  foreach { case (participant, pIndex) =>
                    collectionEventRepository.allForParticipant(participant.id).zipWithIndex.
                      foreach {
                        case (cevent, ceventIndex) =>
                          collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId).
                            foreach { cventType =>

                              (0 to 5).foreach { id =>
                                val uniqueId = 1000 * centreIndex + 100 * pIndex + 10 * ceventIndex + id
                                val inventoryId = f"A$uniqueId%05d"
                                val location = centre.locations.head
                                val specimen = addSpecimen(SpecimenId(hashids.encode(uniqueId.toLong)),
                                                           inventoryId,
                                                           cventType.specimenSpecs.head,
                                                           location)
                                specimenRepository.put(specimen)
                                ceventSpecimenRepository.put(CeventSpecimen(cevent.id, specimen.id))

                                log.debug(s"added specimen: invId: $inventoryId, participant: ${participant.uniqueId}, visitNum: ${cevent.visitNumber}, locationId: ${location.uniqueId}")
                              }
                            }
                      }
                  }
              }
              case _ =>
            }
          }
        }
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures",
                          "org.wartremover.warts.Var",
                          "org.wartremover.warts.TraversableOps"))
  def addBbpspShipments() = {
    if (loadShipmentTestData) {
      log.debug(s"addBbpspShipments")

      /**
       * - creates 3 shipments, each in a different state,
       *
       * - takes all the specimens at the first centre, splits them in to and assigns the first half to the
       *   shipment in CREATED state, and the second half to the shipment in UNPACKED state
       *
       * - takes all the specimens at the second centre and assigns them to the shipment in PACKED state
       */
      studyRepository.getValues.find(s => s.name == "BBPSP").foreach { bbpsp =>
        val v = List("100-Calgary AB", "101-London ON").
          map { name =>
            centreRepository.getValues.
              find { c =>
                (c.name == name) && (c.locations.size > 0)
              }.
              toSuccessNel(s"centre not found or centre has no locaion")
          }.
          sequenceU

        v.foreach { centres =>
          val fromCentre = centres(0)
          val toCentre = centres(1)
          val shipmentMap = Map[EntityState, Shipment](
              ( Shipment.createdState  ->
                 CreatedShipment(id             = ShipmentId(s"test-shipment-1"),
                                 version        = 0,
                                 timeAdded      = DateTime.now,
                                 timeModified   = None,
                                 courierName    = "FedEx",
                                 trackingNumber = "TN1",
                                 fromCentreId   = fromCentre.id,
                                 fromLocationId = fromCentre.locations.head.uniqueId,
                                 toCentreId     = toCentre.id,
                                 toLocationId   = toCentre.locations.head.uniqueId,
                                 timePacked     = None,
                                 timeSent       = None,
                                 timeReceived   = None,
                                 timeUnpacked   = None)),
              ( Shipment.packedState ->
                 PackedShipment(id             = ShipmentId(s"test-shipment-2"),
                                version        = 0,
                                timeAdded      = DateTime.now,
                                timeModified   = None,
                                courierName    = "FedEx",
                                trackingNumber = "TN2",
                                fromCentreId   = fromCentre.id,
                                fromLocationId = fromCentre.locations.head.uniqueId,
                                toCentreId     = toCentre.id,
                                toLocationId   = toCentre.locations.head.uniqueId,
                                timePacked     = Some(DateTime.now),
                                timeSent       = None,
                                timeReceived   = None,
                                timeUnpacked   = None)),
              ( Shipment.unpackedState ->
                 UnpackedShipment(id             = ShipmentId(s"test-shipment-3"),
                                  version        = 0,
                                  timeAdded      = DateTime.now,
                                  timeModified   = None,
                                  courierName    = "FedEx",
                                  trackingNumber = "TN1",
                                  fromCentreId   = fromCentre.id,
                                  fromLocationId = fromCentre.locations.head.uniqueId,
                                  toCentreId     = toCentre.id,
                                  toLocationId   = toCentre.locations.head.uniqueId,
                                  timePacked     = Some(DateTime.now),
                                  timeSent       = Some(DateTime.now),
                                  timeReceived   = Some(DateTime.now),
                                  timeUnpacked   = Some(DateTime.now))))

          shipmentMap.values.foreach(shipmentRepository.put)

          centres.foreach { centre =>
            val locationId = centre.locations.head.uniqueId
            val specimens = specimenRepository.getValues.filter(_.locationId == locationId)
            val halfSpecimenCount = specimens.size / 2

            specimens.zipWithIndex.foreach { case (spc, index) =>
              val shipment =
                if (locationId == centres(0).locations.head.uniqueId) {
                  if (index < halfSpecimenCount) {
                    shipmentMap(Shipment.createdState)
                  } else {
                    shipmentMap(Shipment.unpackedState)
                  }
                } else {
                  shipmentMap(Shipment.packedState)
                }
              log.debug(s"adding specimen to shipment: specimen: $index, ${specimens.size}, ${spc.inventoryId}, ${shipment.trackingNumber}")
              val ss = ShipmentSpecimen(id                  = ShipmentSpecimenId(spc.id.id),
                                        version             = 0L,
                                        timeAdded           = DateTime.now,
                                        timeModified        = None,
                                        shipmentId          = shipment.id,
                                        specimenId          = spc.id,
                                        state               = ShipmentItemState.Present,
                                        shipmentContainerId = None)
              shipmentSpecimenRepository.put(ss)
            }
          }
        }

        if (v.isFailure) {
          log.error("could not add shipments and / or shipment specimens")
        }
      }
    }
  }

  log.debug(s"""|TEST DATA:
                |  mode: ${env.mode}
                |  loadTestData: $loadTestData,
                |  loadSpecimenTestData: $loadSpecimenTestData,
                |  loadShipmentTestData: $loadShipmentTestData""".stripMargin)

  addMultipleUsers
  addMultipleCentres
  addMultipleStudies
  addCollectionEventTypes
  addBbpspParticipants
  addBbpspCevents
  addBbpspSpecimens
  addBbpspShipments
}
