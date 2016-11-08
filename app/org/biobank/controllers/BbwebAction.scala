package org.biobank.controllers

import javax.inject.Inject
import org.biobank.service.AuthToken
import org.biobank.service.users.UsersService
import play.api.Environment
import play.api.http.HttpVerbs
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results.Unauthorized

import scala.concurrent.{ExecutionContext, Future}

class BbwebRequest[A](request: Request[A], val authInfo: AuthenticationInfo)
    extends WrappedRequest(request)

class BbwebAction @Inject()(val env:          Environment,
                            val usersService: UsersService,
                            val authToken:    AuthToken)
                         (implicit ec: ExecutionContext)
    extends ActionBuilder[BbwebRequest]
    with HttpVerbs
    with Security {

  type BbwebRequestBlock[A] = BbwebRequest[A] => Future[Result]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override def invokeBlock[A](request: Request[A], block: BbwebRequestBlock[A]):
      Future[Result] = {
    if (logger.isTraceEnabled) {
      logger.trace(s"invokeBlock: request = $request")
    }

    validateToken(request).fold(
      err => {
        val json = Json.obj("status" -> "error", "message" -> err.list.toList.mkString(", "))
        Future.successful(Unauthorized(json))
      },
      authInfo => {
        val future = block(new BbwebRequest(request, authInfo))
        future.map { result =>
          request.method match {
            case GET | HEAD =>
              result.withHeaders("Cache-Control" -> "max-age: 100")
            case other =>
              result
          }
        }
      }
    )
  }

}
