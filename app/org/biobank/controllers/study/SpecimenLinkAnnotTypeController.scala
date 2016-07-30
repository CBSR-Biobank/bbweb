// package org.biobank.controllers.study

// import org.biobank.domain.ControllerError
// import org.biobank.controllers._
// import org.biobank.service._
// import org.biobank.service.users.UsersService
// import org.biobank.service.study.StudiesService
// import org.biobank.infrastructure.command.StudyCommands._

// import javax.inject.{Inject => javaxInject, Singleton}
// import scala.concurrent.Future
// import play.api.Logger
// import play.api.Play.current
// import scala.language.reflectiveCalls

// class SpecimenLinkAnnotTypeController @javaxInject() (val authToken:      AuthToken,
//                                                       val usersService:   UsersService,
//                                                       val studiesService: StudiesService)
//     extends CommandController
//     with JsonController {

//   def get(specimenLinkTypeId: String, annotTypeId: Option[String]) =
//     AuthAction(parse.empty) { (token, userId, request) =>
//       Logger.debug(s"SpecimenLinkAnnotTypeController.list: specimenLinkTypeId: $specimenLinkTypeId, annotTypeId: $annotTypeId")

//       annotTypeId.fold {
//         validationReply(studiesService.specimenLinkAnnotationTypes(specimenLinkTypeId).map(_.toList))
//       } { id =>
//         validationReply(studiesService.specimenLinkAnnotationTypeWithId(id))
//       }
//     }

//   def addAnnotationType() =
//     commandAction { cmd: AddSpecimenLinkAnnotationTypeCmd =>
//       val future = studiesService.addSpecimenLinkAnnotationType(cmd)
//       validationReply(future)
//     }

//   def updateAnnotationType() =
//     commandAction { cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
//       val future = studiesService.updateSpecimenLinkAnnotationType(cmd)
//       validationReply(future)
//     }

//   def removeAnnotationType(annotationTypeId: String, ver: Long) =
//     AuthActionAsync(parse.empty) { (token, userId, request) =>
//       val cmd = RemoveSpecimenLinkAnnotationTypeCmd(Some(userId.id), annotationTypeId, ver)
//       val future = studiesService.removeSpecimenLinkAnnotationType(cmd)
//       validationReply(future)
//   }

// }
