// package org.biobank.controllers.study

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

// @Singleton
// class CeventAnnotTypeController @javaxInject() (val authToken:      AuthToken,
//                                                 val usersService:   UsersService,
//                                                 val studiesService: StudiesService)
//     extends CommandController
//     with JsonController {

//   def get(cetId: String, annotTypeId : Option[String]) =
//     AuthAction(parse.empty) { (token, userId, request) =>
//       Logger.debug(s"CeventAnnotTypeController.list: cetId: $cetId, annotTypeId: $annotTypeId")

//       annotTypeId.fold {
//         domainValidationReply(studiesService.collectionEventAnnotationTypes(cetId).map(_.toList))
//       } { id =>
//         domainValidationReply(studiesService.collectionEventAnnotationTypeWithId(cetId, id))
//       }
//     }

//   def addAnnotationType() =
//     commandAction { cmd: AddCollectionEventAnnotationTypeCmd =>
//       val future = studiesService.addCollectionEventAnnotationType(cmd)
//       domainValidationReply(future)
//     }

//   def updateAnnotationType() =
//     commandAction { cmd: UpdateCollectionEventAnnotationTypeCmd =>
//       Future.successful(BadRequest("no longer supported"))
//     }

//   def removeAnnotationType(cetId: String, id: String) =
//     AuthActionAsync(parse.empty) { (token, userId, request) =>
//       val cmd =  RemoveCollectionEventAnnotationTypeCmd(Some(userId.id), cetId, id, 0L)
//       val future = studiesService.removeCollectionEventAnnotationType(cmd)
//       domainValidationReply(future)
//     }

// }
