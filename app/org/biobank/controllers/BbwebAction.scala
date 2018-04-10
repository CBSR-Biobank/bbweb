package org.biobank.controllers

import javax.inject.Inject
import org.biobank.services.AuthToken
import org.biobank.services.users.UsersService
import play.api.Environment
import play.api.http.HttpVerbs
import play.api.mvc._
import play.api.mvc.Results.Unauthorized
import scala.concurrent.{ExecutionContext, Future}

class BbwebRequest[A](request: Request[A], val authInfo: AuthenticationInfo)
    extends WrappedRequest[A](request)

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class BbwebAction @Inject()(controllerComponents: ControllerComponents,
                            val env:              Environment,
                            val usersService:     UsersService,
                            val authToken:        AuthToken)
                         (implicit ec: ExecutionContext)
    extends ActionBuilder[BbwebRequest, AnyContent]
    with HttpVerbs
    with Security {

  type BbwebRequestBlock[A] = BbwebRequest[A] => Future[Result]

  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass)

  override protected def executionContext: ExecutionContext = controllerComponents.executionContext

  override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

  override def invokeBlock[A](request: Request[A], block: BbwebRequestBlock[A]): Future[Result] = {
    if (logger.isTraceEnabled) {
      logger.trace(s"invokeBlock: request: $request")
    }

    validateToken(request).fold(
      err => {
        Future.successful(Unauthorized)
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
