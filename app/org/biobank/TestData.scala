package org.biobank

import java.time.OffsetDateTime
import javax.inject.{ Inject, Singleton }
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.access.RoleId._
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.service.PasswordHasher
import org.hashids.Hashids
import play.api.{ Configuration, Environment, Logger, Mode }
import scalaz.Scalaz._

/**
 * Provides initial data to test with.
 */
object TestData {

  val centreData: List[Tuple2[String, String]] = List(
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

  val studyData: List[Tuple2[String, String]] = List(
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

  val userData: List[Tuple2[String, String]] = List(
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
      ("Suzanne Morissette", "suzanne.morissette.chum@ssss.gouv.qc.ca"),
      ("Francine Marsan", "francine.marsan.chum@ssss.gouv.qc.ca"),
      ("Jeanne Bjergo", "jeannebjergo@hcnw.com"),
      ("Larissa Weeks", "larissaweeks@hcnw.com"),
      ("Sharon Fulton", "sharonfulton@hcnw.com"),
      ("Mirjana Maric Viskovic", "maric@ucalgary.ca"),
      ("Paivi Kastell", "paivi.kastell@ppshp.fi"),
      ("Paivi Koski", "paivi.koski@ppshp.fi")
    )

  val ahfemDescription: String =
    s"""|Magnis turpis mollis. Duis commodo libero. Turpis magnis massa morbi cras non mollis, maecenas
        |dictumst venatis augue, rhoncus id non eros nec odio. Ut wisi ullamcorper elit parturient,
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

  val accessUserData: List[Tuple2[String, String]] = List(
      ("study-administrator", "Study Administrator"),
      ("study-user",          "Study User"),
      ("specimen-collector",  "Specimen Collector"),
      ("shipping-admin",      "Shipping Admin"),
      ("shipping-user",       "Shipping User")
    )

  val EventTypeHashids: Hashids      = Hashids("test-data-cevent-types")
}

object BbpspTestData {

  val BbpspStudyId: StudyId        = StudyId("BBPSP_id")
  val CentreNames: List[String]    = List("100-Calgary AB", "101-London ON")
  val NumParticipants: Int         = 3
  val EventTypeNames: List[String] = List("Default Event", "Second Event")

  val ParticipantAnnotationTypeHashids: Hashids = Hashids("bbpsp-participant-annotation-types")
  val EventTypeAnnotationTypeHashids: Hashids   = Hashids("bbpsp-collection-event-annotation-types")
  val EventHashids: Hashids                     = Hashids("bbpsp-collection-events")
  val ParticipantHashids: Hashids               = Hashids("bbpsp-participants")
  val SpecimenHashids: Hashids                  = Hashids("bbpsp-specimen-specs")

  // the slug is temporarily assigned an empty value, once the set is created, the slug is then derived
  // from the name below.
  val CollectionSpecimenDescriptions: Set[CollectionSpecimenDescription] =
    Set(CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(1)),
          slug                        = "",
          name                        = "10 mL Lavender top EDTA tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.WholeBloodEdta,
          maxCount                    = 2, // set to 2 for testing form, set back to 1 for demo
          amount                      = 10.0),
        CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(2)),
          slug                        = "",
          name                        = "10 mL Orange top PAXgene tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.Paxgene,
          maxCount                    = 1,
          amount                      = 10.0),
        CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(3)),
          slug                        = "",
          name                        = "3mL Lavender top EDTA tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.WholeBloodEdta,
          maxCount                    = 1,
          amount                      = 3),
        CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(4)),
          slug                        = "",
          name                        = "4ml lavender top EDTA tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.WholeBloodEdta,
          maxCount                    = 1,
          amount                      = 4),
        CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(5)),
          slug                        = "",
          name                        = "9ml CPDA yellow top tube",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Blood,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.RoomTemperature,
          specimenType                = SpecimenType.WholeBloodEdta,
          maxCount                    = 1,
          amount                      = 9),
        CollectionSpecimenDescription(
          id                          = SpecimenDescriptionId(SpecimenHashids.encode(6)),
          slug                        = "",
          name                        = "Urine cup",
          description                 = None,
          units                       = "mL",
          anatomicalSourceType        = AnatomicalSourceType.Urine,
          preservationType            = PreservationType.FreshSpecimen,
          preservationTemperatureType = PreservationTemperatureType.Plus4celcius,
          specimenType                = SpecimenType.CdpaPlasma,
          maxCount                    = 1,
          amount                      = 15)
    ).map { sd => sd.copy(slug = Slug(sd.name)) }

  val EventTypeAnnotationTypes: Set[AnnotationType] =
    Set(
      AnnotationType(
        id            = AnnotationTypeId(EventTypeAnnotationTypeHashids.encode(1)),
        slug          = "",
        name          = "Phlebotomist",
        description   = None,
        valueType     = AnnotationValueType.Text,
        maxValueCount = None,
        options       = Seq.empty[String],
        required      = true),
      AnnotationType(
        id            = AnnotationTypeId(EventTypeAnnotationTypeHashids.encode(2)),
        slug          = "",
        name          = "Consent",
        description   = None,
        valueType     = AnnotationValueType.Select,
        maxValueCount = Some(2),
        options       = Seq("Surveillance", "Genetic Predisposition", "Previous Samples", "Genetic Mutation"),
        required      = true)
    ).map { at => at.copy(slug = Slug(at.name)) }

  val ParticipantAnnotationTypes: Set[AnnotationType] =
    Set(
      AnnotationType(
        id            = AnnotationTypeId(ParticipantAnnotationTypeHashids.encode(1)),
        slug          = "",
        name          = "Date of birth",
        description   = None,
        valueType     = AnnotationValueType.DateTime,
        maxValueCount = None,
        options       = Seq.empty[String],
        required      = true),
      AnnotationType(
        id            = AnnotationTypeId(ParticipantAnnotationTypeHashids.encode(2)),
        slug          = "",
        name          = "Gender",
        description   = None,
        valueType     = AnnotationValueType.Select,
        maxValueCount = Some(1),
        options       = Seq("Female", "Male"),
        required      = true)
    ).map { at => at.copy(slug = Slug(at.name)) }
}

/**
 * Provides initial data to test with. Ideally these methods should only be called for development builds.
 */
@Singleton
class TestData @Inject() (config:         Configuration,
                          env:            Environment,
                          passwordHasher: PasswordHasher) {

  import TestData._

  case class ParticipantData(participant: Participant, eventData: List[CollectionEventData])

  case class CollectionEventData(event: CollectionEvent, specimens: List[Specimen])

  val log: Logger = Logger(this.getClass)

  lazy val participantData: List[ParticipantData] = createParticipants

  private val loadTestData =
    (env.mode == Mode.Dev) && config.get[Boolean]("application.testData.load")

  private val loadSpecimenTestData =
    (env.mode == Mode.Dev) && loadTestData && config.get[Boolean]("application.testData.loadSpecimens")

  private val loadShipmentTestData =
    (env.mode == Mode.Dev) && loadSpecimenTestData && config.get[Boolean]("application.testData.loadShipments")

  private val loadAccessTestData =
    (env.mode == Mode.Dev) && config.get[Boolean]("application.testData.loadAccessData")

  def testUsers(): List[User] = {
    if (!loadTestData) {
      List.empty[User]
    } else {
      log.debug("testUsers")

      val plainPassword = "testuser"
      val salt = passwordHasher.generateSalt
      val hashids = Hashids("test-data-users")

      userData.zipWithIndex.map { case((name, email), index) =>
        ActiveUser(id           = UserId(hashids.encode(index.toLong)),
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = Slug(name),
                   name         = name,
                   email        = email,
                   password     = passwordHasher.encrypt(plainPassword, salt),
                   salt         = salt,
                   avatarUrl    = None)
      }
    }
  }

  def testCentres(): List[Centre] = {
    if (!loadTestData) {
      List.empty[Centre]
    } else {
      log.debug("testCentres")

      centreData.map { case (name, description) =>
        val locations = {
          if (name == "100-Calgary AB") {
            Set(Location(id             = LocationId(s"${name}_id:Primary"),
                         slug           = "",
                         name           = "Primary",
                         street         = "1403 29 St NW",
                         city           = "Calgary",
                         province       = "Alberta",
                         postalCode     = "T2N 2T9",
                         poBoxNumber    = None,
                         countryIsoCode = "CA"))
          } else if (name == "101-London ON") {
            Set(Location(id             = LocationId(s"${name}_id:Primary"),
                         slug           = "",
                         name           = "Primary",
                         street         = "London Health Sciences Center, University Hospital, Rm A3-222B, 339 Windermere Road",
                         city           = "London",
                         province       = "Ontario",
                         postalCode     = "N0L 1W0",
                         poBoxNumber    = None,
                         countryIsoCode = "CA"))
          } else {
            Set.empty[Location]
          }
        }.map { l => l.copy(slug = Slug(l.id.id)) }

        if ((name == "100-Calgary AB") || (name == "101-London ON")) {
          EnabledCentre(id           = CentreId(s"${name}_id"),
                        version      = 0L,
                        timeAdded    = Global.StartOfTime,
                        timeModified = None,
                        slug         = Slug(name),
                        name         = name,
                        description  = Some(description),
                        studyIds     = Set(BbpspTestData.BbpspStudyId),
                        locations    = locations).asInstanceOf[Centre]
        } else {
          DisabledCentre(id           = CentreId(s"${name}_id"),
                         version      = 0L,
                         timeAdded    = Global.StartOfTime,
                         timeModified = None,
                         slug         = Slug(name),
                         name         = name,
                         description  = Some(description),
                         studyIds     = Set(BbpspTestData.BbpspStudyId),
                         locations    = locations).asInstanceOf[Centre]
        }
      }
    }
  }

  def testStudies(): List[Study] = {
    if (!loadTestData) {
      List.empty[Study]
    } else {
      log.debug("testStudies")

      studyData.map { case (name, description) =>
        val descMaybe = if (name == "AHFEM") Some(s"$description\n\n$ahfemDescription")
                        else Some(description)

        if (name == "BBPSP") {
          EnabledStudy(id              = StudyId(s"${name}_id"),
                       version         = 0L,
                       timeAdded       = Global.StartOfTime,
                       timeModified    = None,
                       slug            = Slug(name),
                       name            = name,
                       description     = descMaybe,
                       annotationTypes = BbpspTestData.ParticipantAnnotationTypes).asInstanceOf[Study]
        } else {
          DisabledStudy(id              = StudyId(s"${name}_id"),
                        version         = 0L,
                        timeAdded       = Global.StartOfTime,
                        timeModified    = None,
                        slug            = Slug(name),
                        name            = name,
                        description     = descMaybe,
                        annotationTypes = BbpspTestData.ParticipantAnnotationTypes).asInstanceOf[Study]
        }
      }
    }
  }

  def testEventTypes(): List[CollectionEventType] = {
    if (!loadTestData) {
      List.empty[CollectionEventType]
    } else {
      log.debug("testEventTypes")

      BbpspTestData.EventTypeNames.zipWithIndex.map { case (name, index) =>
        val id = CollectionEventTypeId(EventTypeHashids.encode(index.toLong))
        val annotationTypes =
          if (name == BbpspTestData.EventTypeNames(0)) BbpspTestData.EventTypeAnnotationTypes
          else Set.empty[AnnotationType]

        CollectionEventType(studyId              = BbpspTestData.BbpspStudyId,
                            id                   = id,
                            version              = 0L,
                            timeAdded            = Global.StartOfTime,
                            timeModified         = None,
                            slug                 = Slug(name),
                            name                 = name,
                            description          = None,
                            recurring            = true,
                            specimenDescriptions = BbpspTestData.CollectionSpecimenDescriptions,
                            annotationTypes      = annotationTypes)
      }
    }
  }

  def testParticipants(): List[Participant] =
    participantData.map { pd =>  pd.participant }

  def testEvents(): List[CollectionEvent] =
    participantData.flatMap(_.eventData.map(_.event))

  def testSpecimens(): List[Specimen] =
    participantData.flatMap(_.eventData.flatMap(_.specimens))

  def testCeventSpecimens(): List[CeventSpecimen] =
    participantData.flatMap(
      _.eventData.flatMap { eventData =>
        eventData.specimens.map { specimen =>
          CeventSpecimen(eventData.event.id, specimen.id)
        }
      }
    )

  @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures",
                          "org.wartremover.warts.Var",
                          "org.wartremover.warts.TraversableOps"))
  def testShipments(): List[Shipment] = {
    if (!loadShipmentTestData) {
      List.empty[Shipment]
    } else {
      log.debug(s"testShipments")

      /*
       * - creates 3 shipments, each in a different state
       */
      val fromCentreId   = CentreId("100-Calgary AB_id")
      val fromLocationId = LocationId(s"${fromCentreId}:Primary")
      val toCentreId     = CentreId("101-London ON_id")
      val toLocationId   = LocationId(s"${toCentreId}:Primary")

      List[Shipment](
        CreatedShipment(id             = ShipmentId(s"test-shipment-created"),
                        version        = 0,
                        timeAdded      = Global.StartOfTime,
                        timeModified   = None,
                        courierName    = "FedEx",
                        trackingNumber = "TN1",
                        fromCentreId   = fromCentreId,
                        fromLocationId = fromLocationId,
                        toCentreId     = toCentreId,
                        toLocationId   = toLocationId,
                        timePacked     = None,
                        timeSent       = None,
                        timeReceived   = None,
                        timeUnpacked   = None,
                        timeCompleted  = None),
        PackedShipment(id             = ShipmentId(s"test-shipment-packed"),
                       version        = 0,
                       timeAdded      = Global.StartOfTime,
                       timeModified   = None,
                       courierName    = "FedEx",
                       trackingNumber = "TN2",
                       fromCentreId   = fromCentreId,
                       fromLocationId = fromLocationId,
                       toCentreId     = toCentreId,
                       toLocationId   = toLocationId,
                       timePacked     = Some(OffsetDateTime.now),
                       timeSent       = None,
                       timeReceived   = None,
                       timeUnpacked   = None,
                       timeCompleted  = None),
        UnpackedShipment(id             = ShipmentId(s"test-shipment-unpacked"),
                         version        = 0,
                         timeAdded      = Global.StartOfTime,
                         timeModified   = None,
                         courierName    = "FedEx",
                         trackingNumber = "TN3",
                         fromCentreId   = fromCentreId,
                         fromLocationId = fromLocationId,
                         toCentreId     = toCentreId,
                         toLocationId   = toLocationId,
                         timePacked     = Some(OffsetDateTime.now),
                         timeSent       = Some(OffsetDateTime.now),
                         timeReceived   = Some(OffsetDateTime.now),
                         timeUnpacked   = Some(OffsetDateTime.now),
                         timeCompleted  = None)
      )
    }
  }

  def testShipmentSpecimens(): List[ShipmentSpecimen] = {
    if (!loadShipmentTestData) {
      List.empty[ShipmentSpecimen]
    } else {
      log.debug("testShipmentSpecimens")

      /*
       * - takes all the specimens at the first centre, splits them in two and assigns the first half to the
       *   shipment in CREATED state, and the second half to the shipment in UNPACKED state
       *
       * - takes all the specimens at the second centre and assigns them to the shipment in PACKED state
       */
      val fromCentreId = CentreId("100-Calgary AB_id")
      val fromLocationId = LocationId(s"${fromCentreId}:Primary")
      val specimens = testSpecimens
      val halfSpecimenCount = specimens.filter(_.locationId == fromLocationId).size / 2

      specimens.zipWithIndex.map { case (specimen, index) =>
        val shipmentId =
          if (specimen.locationId == fromLocationId) {
            if (index < halfSpecimenCount) ShipmentId("test-shipment-created")
            else ShipmentId("test-shipment-unpacked")
          } else {
            ShipmentId("test-shipment-packed")
          }

        ShipmentSpecimen(id                  = ShipmentSpecimenId(specimen.id.id),
                         version             = 0L,
                         timeAdded           = Global.StartOfTime,
                         timeModified        = None,
                         shipmentId          = shipmentId,
                         specimenId          = specimen.id,
                         state               = ShipmentItemState.Present,
                         shipmentContainerId = None)
      }
    }
  }

  private def createParticipants(): List[ParticipantData] = {
    if (!loadSpecimenTestData) {
      List.empty[ParticipantData]
    } else {
      log.debug("createParticipants")

      (0 to BbpspTestData.NumParticipants)
        .map { index =>
          val id = ParticipantId(BbpspTestData.ParticipantHashids.encode(index.toLong))
          val uniqueId = f"P$index%05d"
          val participant =  Participant(id           = id,
                                         studyId      = BbpspTestData.BbpspStudyId,
                                         version      = 0L,
                                         timeAdded    = Global.StartOfTime,
                                         timeModified = None,
                                         slug         = Slug(uniqueId),
                                         uniqueId     = uniqueId,
                                         annotations  = Set.empty[Annotation])
          ParticipantData(participant, createCeventData(participant))
        }
        .toList
    }
  }

  private def createCeventData(participant: Participant): List[CollectionEventData] = {
    log.debug(s"createCevents")

    BbpspTestData.EventTypeNames.zipWithIndex.map { case (eventTypeName, eventTypeIndex) =>
      val eventTypeId = CollectionEventTypeId(EventTypeHashids.encode(eventTypeIndex.toLong))
      val reverseHash = BbpspTestData.ParticipantHashids.decode(participant.id.id)
      val id = CollectionEventId(BbpspTestData.EventHashids.encode((reverseHash :+ eventTypeIndex.toLong):_*))

      val event = CollectionEvent(id                    = id,
                                  participantId         = participant.id,
                                  collectionEventTypeId = eventTypeId,
                                  version               = 0L,
                                  timeAdded             = Global.StartOfTime,
                                  timeModified          = None,
                                  slug                  = Slug(id.id),
                                  timeCompleted         = OffsetDateTime.now.minusDays(1),
                                  visitNumber           = eventTypeIndex + 1,
                                  annotations           = Set.empty[Annotation])

      CollectionEventData(event, createSpecimens(event))
    }
  }

  /*
   * Creates two specimens per collection event specimen description, one from each centre associated with
   * BBPSP.
   */
  private def createSpecimens(event: CollectionEvent): List[Specimen] = {
    if (!loadSpecimenTestData) {
      List.empty[Specimen]
    } else {
      BbpspTestData.CentreNames.zipWithIndex.flatMap { case (centreName, centreIndex) =>
        val centreId = CentreId(s"${centreName}_id")
        val locationId = LocationId(s"${centreId}:Primary")

        BbpspTestData.CollectionSpecimenDescriptions.zipWithIndex.map { case (specimenDesc, specimenIndex) =>
          val reverseHash = BbpspTestData.EventHashids.decode(event.id.id)
          val participantIndex = reverseHash(0)
          val eventIndex = reverseHash(1)
          val uniqueId = 1000L * centreIndex + 100 * participantIndex + 10 * eventIndex + specimenIndex
          val inventoryId = f"A$uniqueId%05d"
          val id = SpecimenId(BbpspTestData.SpecimenHashids.encode(uniqueId))

          UsableSpecimen(id                    = id,
                         version               = 0L,
                         timeAdded             = Global.StartOfTime,
                         timeModified          = None,
                         slug                  = Slug(inventoryId),
                         inventoryId           = inventoryId,
                         specimenDescriptionId = specimenDesc.id,
                         originLocationId      = locationId,
                         locationId            = locationId,
                         containerId           = None,
                         positionId            = None,
                         timeCreated           = OffsetDateTime.now.minusDays(1),
                         amount                = BigDecimal(0.1))
        }
      }
    }
  }

  /**
   * This is only to demo the User Access / Permissions. It should be removed for production servers.
   */
  def accessUsers(): List[User] = {
    if (loadAccessTestData) {
      accessUserData.map { case (id, name) =>
        ActiveUser(
          id           = UserId(id),
          version      = 0L,
          timeAdded    = Global.StartOfTime,
          timeModified = None,
          slug         = Slug(name),
          name         = name,
          email        = s"$id@admin.com",
          password     = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.0nut7rysaLcKpbalteFuDN8uIwaojCa",
          salt         = "$2a$10$Kvl/h8KVhreNDiiOd0XiB.",
          avatarUrl    = None)
      }
    } else {
      List.empty[User]
    }
  }

  def testRoles(): List[Tuple2[UserId, RoleId]] = {
    if (loadAccessTestData) {
      List((UserId("study-administrator"), RoleId.StudyAdministrator),
           (UserId("study-user"),          RoleId.StudyUser),
           (UserId("specimen-collector"),  RoleId.SpecimenCollector),
           (UserId("shipping-admin"),      RoleId.ShippingAdministrator),
           (UserId("shipping-user"),       RoleId.ShippingUser))
    } else {
      List.empty[Tuple2[UserId, RoleId]]
    }
  }

  def testMemberships(): List[Membership] = {
    if (!loadAccessTestData) {
      List.empty[Membership]
    } else {
      val studyUserIds = Set("study-administrator",
                             "study-user",
                             "specimen-collector").map(UserId(_))

      val centreUserIds = Set("shipping-admin",
                              "shipping-user").map(UserId(_))

      List(
        Membership(id = MembershipId("all-studies-membership"),
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = "",
                   name         = "All studies",
                   description  = None,
                   userIds      = studyUserIds,
                   studyData    = MembershipEntitySet(true, Set.empty[StudyId]),
                   centreData   = MembershipEntitySet(false, Set.empty[CentreId])),
        Membership(id = MembershipId("all-centres-membership"),
                   version      = 0L,
                   timeAdded    = Global.StartOfTime,
                   timeModified = None,
                   slug         = "",
                   name         = "All centres",
                   description  = None,
                   userIds      = centreUserIds,
                   studyData    = MembershipEntitySet(false, Set.empty[StudyId]),
                   centreData   = MembershipEntitySet(true, Set.empty[CentreId]))
      ).map(m => m.copy(slug = Slug(m.name)))
    }
  }

  log.debug(s"""|TEST DATA:
                |  mode:                 ${env.mode}
                |  loadTestData:         $loadTestData,
                |  loadSpecimenTestData: $loadSpecimenTestData,
                |  loadShipmentTestData: $loadShipmentTestData,
                |  loadAccessTestData:   $loadAccessTestData""".stripMargin
  )

}
