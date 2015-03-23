package org.biobank

import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.infrastructure._
import org.biobank.service.PasswordHasher

import org.joda.time.DateTime
import play.api.Logger
import scaldi.{Injectable, Injector}
import akka.actor.ActorSystem
import org.slf4j.LoggerFactory

/**
 * Provides initial data to test with. Ideally these methods should only be called for developemnt builds.
 */
object TestData extends Injectable {

  val log = LoggerFactory.getLogger(this.getClass)

  val configPath = "application.loadTestData"

  val centreData = List(
    ("fd0c47dfecce46549be0220066f88aea", "CL1-Foothills", "CL1-Foothills"),
    ("a176700d8bcd413787b09336146bd484", "CL1-Heritage", "CL1-Heritage"),
    ("213d322c2ea74738a4fc716e56e99c5a", "CL1-Sunridge", "CL1-Sunridge"),
    ("5f96dac71fdf482096ca6647d839947f", "CL2-Children Hosp", "CL2-Alberta's Children's Hospital"),
    ("5430762309d34371bec10619fd2a0b25", "ED1-UofA", "ED1-UofA"),
    ("9a1cf03368364345b100066d3e9ce2b7", "OT2-Children Hosp", "OT2-Children's Hospital of Eastern Ontario"),
    ("84906909311147c18b085473b4259fbc", "QB1-Enfant-Jesus", "QB1-Hopital Enfant-Jesus"),
    ("1a0a5060cc5b4982a0c4697a1956e75a", "RD1-Red Deer Hosp", "RD1-Red Deer Regional Hospital"),
    ("a0722517d8ff454ba2b0c9a5fd0476bd", "SB1-St John NB Hosp", "SB1-Saint Johns NB Regional Hospital"),
    ("59a485ab525644e0babea2c5dcb7ca2b", "SD1-Sudbury Hosp", "SD1-Sudbury Regional Hospital"),
    ("db034b3a7b1b47a1a5991d8e8f3426a5", "SF1-Health NFLD", "SF1-Health Science Center"),
    ("8e4e2bd96e4a4a638182d7d27b10e374", "SP1-St Therese Hosp", "SP1-St Therese Hospital"),
    ("41a114187e3d43c59871f8b777681f7c", "SS1-Royal Hosp", "SS1-Royal University Hospital"),
    ("6a84942115d2465caf63b994004adefc", "TH1-Regional Hosp", "TH1-Thunder Bay Regional Hospital"),
    ("b25d04e7c66641daa2bf25fa4392ea11", "TR1-St Mikes", "TR1-St Michael's Hospital"),
    ("7cca56f87a1c4a52a11d83b3c37a6745", "VN1-St Paul", "VN1-St Paul's Hospital"),
    ("436ebfffb39d4274960ca860bf7f0d70", "VN2-Childrens Hosp", "VN2-BC Women and Children's Hospital"),
    ("7e92a3b606e9454ea0c723017d2d8b31", "WL1-Westlock Hosp", "WL1-Westlock Health Care Center"),
    ("96efbb19a8f04719a8fd0718eb71d3c6", "WN1-Cancer Care", "WN1-Cancer Care Manitoba"),
    ("31d72b83e44b4d659c85b33e34ebf892", "CL1-Foothills TRW", "CL1-Foothills TRW"),
    ("5cff3b52ee614955b3b2678c1bd8db6b", "CBSR", "Canadian BioSample Repository"),
    ("03cfe59222ab41368dc7a53304a0c1eb", "Calgary-F", "Calgary Foothills"),
    ("526b806e611f4e42887f7b73b52f1a00", "100-Calgary AB", "100-Calgary Alberta Refine"),
    ("6ed7fdc7eed34e6f953c28a4039c4303", "College Plaza UofA", "College Plaza UofA"),
    ("5f485e9b9e084a8d89c91ca0381cda93", "101-London ON", "101-London Ontario Refine"),
    ("e00765c96c994788a822e5028b5c338b", "102-Newmarket ON", "102-Newmarket Ontario Refine"),
    ("0e667d31c7244722a952a00333ee1847", "103-Montreal QC", "103-Montreal Quebec Refine"),
    ("126760ec653f49f79692419dee1027d2", "104-Montreal QC", "104-Montreal Quebec Refine"),
    ("374771ef392a4df6a60d20777bcf9630", "105-Victoria BC", "105-Victoria British Columbia refine"),
    ("c0b93f4229a94d84820b17b46085d0c6", "106-Quebec City QC", "106-Quebec City Quebec Refine"),
    ("70e5ff857c81434096d6d19db83abfc1", "CaRE ED1", "CaRE ED1"),
    ("fb949aa553a6488bae344f55378110d3", "200-Spokane WA", "200-Spokane Washington REFINE"),
    ("544b1d2f33f84835b852c4772e1090a2", "201-Erie PA", "201-Erie Pennsylvania REFINE"),
    ("a3e3c42cdce949dc8e76eba9dd130c92", "202-Cherry Hill NJ", "202-Cherry Hill New Jersey REFINE"),
    ("b554f8bce4f647689521dce66437c278", "203-West Des Moines IA", "203-West Des Moines Iowa REFINE"),
    ("c2961133850c4501bd91661be2adbcc4", "204-Evansville IN", "204-Evansville Indiana REFINE"),
    ("3177b52571144ff0a91c38619a4b4af2", "205-Southfield MI", "205-Southfield Michigan REFINE"),
    ("2bbce120784345889608b10b0684d763", "206 A -Nashville TN", "206 A -Nashville Tennessee REFINE"),
    ("66a0ad4939d842ab9d315c37441f8f43", "207-Amarillo TX", "207-Amarillo Texas REFINE"),
    ("1d218c3d731a4a78bafeedc100c184c7", "300-Oulu FI", "300-Oulu Finland REFINE"),
    ("4abb6fa792ea47fcbc0db343aec3141c", "ED1-GNH", "ED1-GNH"),
    ("67d0fa657720475a8d0e5527de6d93b5", "KIDNI CL1", "KIDNI Calgary")
  )

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

  val sgIds = Seq("289561b5e6934bebb8c4702ecb6799e1",
                  "83f46b59cff24a688f832994fc0b0ca9",
                  "1a833e5ef5a146018d6e00e2600aa39c",
                  "6a3685e622a64f0db94a97c1be3686ff",
                  "97d61897c4eb48a986be425ecd91b421")

  val cetIds = Seq("6f5de670a4844e24a8dcff35b346f868",
                   "659d4150925147e1864f581854c36b89",
                   "2f40c7d5deda41dfbd2c29a7eaa14b00",
                   "756e0ca822044ea4a959b4516e0f97aa",
                   "84e2c48d6ae7478f8b623cc59607bbb3")

  val userData = List(
    ("98cd257929e74a03bbef4a66067614cc", "Aaron Peck", "aaron.peck@ualberta.ca"),
    ("6e2385f6466d408697bc4c0d1c2b4e66", "Nelson Loyola", "loyola@ualberta.ca"),
    ("f417948d108c468eaa7ef0c2531de0ef", "Luisa Franco", "lfrancor@ucalgary.ca"),
    ("b94257c9c6274a5597ca32e9f5fb0875", "Corazon Oballo", "coballo@ucalgary.ca"),
    ("31220b17be584b7fb73f31acf91e9d68", "Amie Lee", "amie1@ualberta.ca"),
    ("7fff64b01bc24977a421f6c3654bdf47", "Lisa Tanguay", "lisa.tanguay@ualberta.ca"),
    ("81ec2e59d0244e7abf52774c0efef969", "Darlene Ramadan", "ramadan@ucalgary.ca"),
    ("40f875bda98547438b7babc8070f1cba", "Juline Skripitsky", "Jskrip@biosample.ca"),
    ("c56bea77bd7a4fb1a38429754521dffc", "Leslie Jackson Carter", "jacksola@ucalgary.ca"),
    ("1ccd4ca5f3034fb8a44362918a5e6c57", "Thiago Oliveira", "toliveir@ucalgary.ca"),
    ("ffc3482d537e446dbc6ca12cc6b99b92", "Rozsa Sass", "rsas@ucalgary.ca"),
    ("a2866a9ae6fa41ec8ab992ede6247f10", "Margaret Morck", "mmorck@ucalgary.ca"),
    ("3b4928f6d0b14fff89a87b884a1ca5b9", "Kristan Nagy", "nagy1@ualberta.ca"),
    ("0aa1e54d803c405399dc8acc1403565e", "Bruce Ritchie", "bruce.ritchie@ualberta.ca"),
    ("7f5bff95c40846a39b9f45b7745a29c2", "Matthew Klassen", "mwklasse@ualberta.ca"),
    ("74ce097f11b34fe8aa2b46d9d10da05f", "Marleen Irwin", "mirwin@ualberta.ca"),
    ("a785a42f21514d97833cca4c20085457", "Millie Silverstone", "millie.silverstone@me.com"),
    ("f3b7575623d84b5b8dd22ff7769d25f5", "Trevor Soll", "tsoll@ualberta.ca"),
    ("60953dfe532842a0bcebeb9c786b6803", "Stephanie Wichuk", "stephaniewichuk@med.ualberta.ca"),
    ("0b8f58b3542748fcb26cf5594391ddce", "Deborah Parfett", "dparfett@catrials.org"),
    ("e41f6d17d67c404e997cc77ce62f4175", "Samantha Taylor", "samantha.taylor@albertahealthservices.ca"),
    ("b9bd92bd6879493384976147de1325bf", "Martine Bergeron", "martine.bergeron@crchum.qc.ca"),
    ("6107efa2aece48ef8d3b6bca9f13af85", "Isabelle Deneufbourg", "isabelle.deneufbourg@criucpq.ulaval.ca"),
    ("3fa89a208baf42709588337bdeb35a32", "Colin Coros", "coros@ualberta.ca"),
    ("f431d76d79d743329a55859b95a1a3f4", "Ray Vis", "rvis@ualberta.ca"),
    ("72469d992e0544ed868348a4a7ddbffb", "Suzanne Morissette", "suzanne.morissette.chum@ssss.gouv.qc.ca"),
    ("f6dd6947b4e24397b2a2a49c7eafd8cd", "Francine Marsan", "francine.marsan.chum@ssss.gouv.qc.ca"),
    ("2fe94da331ca4e65bf582c8b5563f601", "Jeanne Bjergo", "jeannebjergo@hcnw.com"),
    ("33dea4db597843b996730ba6c19ba721", "Larissa Weeks", "larissaweeks@hcnw.com"),
    ("2602abaa2d6b4de39df2d2d6aa9c227b", "Sharon Fulton", "sharonfulton@hcnw.com"),
    ("28c39db836634627948fac3bbb88726f", "Mirjana Maric Viskovic", "maric@ucalgary.ca"),
    ("2295eef3d29d4de79997ebff9a962209", "Paivi Kastell", "paivi.kastell@ppshp.fi"),
    ("88346afcb2884e53853a25da6930fb64", "Paivi Koski", "paivi.koski@ppshp.fi")
  )

  def addMultipleCentres(implicit injector: Injector): Unit = {
    val actorSystem = inject [ActorSystem]

    if (actorSystem.settings.config.hasPath(TestData.configPath)
      && actorSystem.settings.config.getBoolean(TestData.configPath)) {
      val centreRepository = inject [CentreRepository]

      Logger.debug("addMultipleCentres")

      val centres = centreData.map { case (id, name, description) =>
        val centre: Centre = DisabledCentre(
          id           = CentreId(id),
          version      = 0L,
          timeAdded    = DateTime.now,
          timeModified = None,
          name         = name,
          description  = Some(description)
        )
        centreRepository.put(centre)
      }
    }
  }

  def addMultipleStudies(implicit injector: Injector): Unit = {
    val actorSystem = inject [ActorSystem]

    if (actorSystem.settings.config.hasPath(TestData.configPath)
      && actorSystem.settings.config.getBoolean(TestData.configPath)) {
      val studyRepository = inject [StudyRepository]

      Logger.debug("addMultipleStudies")

      val studies = studyData.map { case (id, name, description) =>
        val study: Study = DisabledStudy(id           = StudyId(id),
                                         version      = 0L,
                                         timeAdded    = DateTime.now,
                                         timeModified = None,
                                         name         = name,
                                         description  = Some(description)
        )
        studyRepository.put(study)
      }

      addSpecimenGroups
      addCollectionEvents
      addParticipantAnnotationTypes

      val specimenGroupRepository = inject [SpecimenGroupRepository]
      val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

      specimenGroupRepository.getValues.foreach { sg =>
        studyRepository.getDisabled(sg.studyId).fold(
          err => Logger.error(s"disabled study not found: $err"),
          study => {
            val valid = study.enable(
              specimenGroupRepository.allForStudy(study.id).size,
              collectionEventTypeRepository.allForStudy(study.id).size)
            valid.fold(
              err => Logger.error(err.list.mkString(",")),
              study => {
                studyRepository.put(study)
                Logger.info(s"study ${study.name} enabled")
              }
            )
          }
        )
      }
    }
  }

  def addSpecimenGroups(implicit injector: Injector) = {
    val studyRepository = inject [StudyRepository]
    val specimenGroupRepository = inject [SpecimenGroupRepository]

    Logger.debug("addSpecimenGroups")

    studyRepository.getValues.take(sgIds.size).zip(sgIds).foreach { case (study, sgId) =>
      val sg = SpecimenGroup(studyId                     = study.id,
                             id                          = SpecimenGroupId(sgId),
                             version                     = 0L,
                             timeAdded                   = DateTime.now,
                             timeModified                = None,
                             name                        = study.name + " SG",
                             description                 = None,
                             units                       = "mL",
                             anatomicalSourceType        = AnatomicalSourceType.Blood,
                             preservationType            = PreservationType.FrozenSpecimen,
                             preservationTemperatureType = PreservationTemperatureType.Minus80celcius,
                             specimenType                = SpecimenType.Rna)

      specimenGroupRepository.put(sg)
    }
  }

  def addCollectionEvents(implicit injector: Injector) = {
    Logger.debug("addCollectionEvents")

    val studyRepository = inject [StudyRepository]
    val specimenGroupRepository = inject [SpecimenGroupRepository]
    val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

    specimenGroupRepository.getValues.zip(cetIds).foreach { case (sg, cetId) =>
      studyRepository.getDisabled(sg.studyId).fold(
        err => Logger.error(s"disabled study not found: $err"),
        study => {
          val sgData = CollectionEventTypeSpecimenGroupData(specimenGroupId = sg.id.id,
                                                            maxCount        = 1,
                                                            amount          = Some(0.1))
          val cet = CollectionEventType(studyId            = study.id,
                                        id                 = CollectionEventTypeId(cetId),
                                        version            = 0L,
                                        timeAdded          = DateTime.now,
                                        timeModified       = None,
                                        name               = study.name + " CET",
                                        description        = None,
                                        recurring          = true,
                                        specimenGroupData  = List(sgData),
                                        annotationTypeData = List.empty)
          collectionEventTypeRepository.put(cet)
          ()
        }
      )
    }
  }

  def addParticipantAnnotationTypes(implicit injector: Injector) = {
    Logger.debug("addParticipantAnnotationTypes")

    val studyRepository = inject [StudyRepository]
    val patRepository = inject [ParticipantAnnotationTypeRepository]
    val studyNames = List("BBPSP")

    studyNames.foreach { studyName =>
      studyRepository.getValues.find(s => s.name == studyName) match {
        case None =>
          Logger.error(s"addParticipantAnnotationTypes: study with name not found: $studyName")
        case Some(study)  =>
          val patList = List(
            ParticipantAnnotationType(
              studyId       = study.id,
              id            = AnnotationTypeId("31674c709c364b2e8ad0d52d1f44e5f8"),
              version       = 0L,
              timeAdded     = DateTime.now,
              timeModified  = None,
              name          = "BBPSP PAT Text",
              description   = None,
              valueType     = AnnotationValueType.Text,
              maxValueCount = None,
              options       = Seq.empty,
              required      = true),
            ParticipantAnnotationType(
              studyId       = study.id,
              id            = AnnotationTypeId("1badaa5943694cdf8a2b4b19922efbd1"),
              version       = 0L,
              timeAdded     = DateTime.now,
              timeModified  = None,
              name          = "BBPSP PAT Number",
              description   = None,
              valueType     = AnnotationValueType.Number,
              maxValueCount = None,
              options       = Seq.empty,
              required      = true),
            ParticipantAnnotationType(
              studyId       = study.id,
              id            = AnnotationTypeId("e6d4576cf8f1400f9779354629ec8a72"),
              version       = 0L,
              timeAdded     = DateTime.now,
              timeModified  = None,
              name          = "BBPSP PAT DateTime",
              description   = None,
              valueType     = AnnotationValueType.DateTime,
              maxValueCount = None,
              options       = Seq.empty,
              required      = true),
            ParticipantAnnotationType(
              studyId       = study.id,
              id            = AnnotationTypeId("390e28e0741b4f7fa1cf02d58d3a3033"),
              version       = 0L,
              timeAdded     = DateTime.now,
              timeModified  = None,
              name          = "Select single",
              description   = None,
              valueType     = AnnotationValueType.Select,
              maxValueCount = Some(1),
              options       = Seq("option1", "option2"),
              required      = true),
            ParticipantAnnotationType(
              studyId       = study.id,
              id            = AnnotationTypeId("a422a68aca2848ceb0bc6d9eeeab47e6"),
              version       = 0L,
              timeAdded     = DateTime.now,
              timeModified  = None,
              name          = "Select multiple",
              description   = None,
              valueType     = AnnotationValueType.Select,
              maxValueCount = Some(2),
              options       = Seq("option1", "option2", "option3"),
              required      = true)
          )
          patList.foreach { pat => patRepository.put(pat) }
      }
    }

  }

  def addMultipleUsers(implicit injector: Injector) = {
    val actorSystem = inject [ActorSystem]

    if (actorSystem.settings.config.hasPath(TestData.configPath)
      && actorSystem.settings.config.getBoolean(TestData.configPath)) {
      Logger.debug("addMultipleUsers")

      val userRepository = inject [UserRepository]
      def passwordHasher = inject [PasswordHasher]
      val plainPassword = "testuser"
      val salt = passwordHasher.generateSalt

      val users = userData.map { case(id, name, email) =>
        val user: User = ActiveUser(
          id           = UserId(id),
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
