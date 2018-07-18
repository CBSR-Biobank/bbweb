package org.biobank.controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import com.mohiva.play.silhouette.api.actions._
import org.biobank.utils.auth.DefaultEnv
import play.api.http.HttpVerbs
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

class BbwebAction @Inject()(controllerComponents: ControllerComponents,
                            silhouette: Silhouette[DefaultEnv])
    extends ActionBuilder[({ type R[B] = SecuredRequest[DefaultEnv, B] })#R, AnyContent]
    with HttpVerbs
{
  type BbwebRequestBlock[A] = SecuredRequest[DefaultEnv, A] => Future[Result]

  override protected def executionContext: ExecutionContext = controllerComponents.executionContext

  override def parser: BodyParser[AnyContent] = controllerComponents.parsers.defaultBodyParser

  private val requestHandler = silhouette.SecuredRequestHandler

  @SuppressWarnings(Array("org.wartremover.warts.ExplicitImplicitTypes"))
  override def invokeBlock[A](request: Request[A], block: BbwebRequestBlock[A]): Future[Result] = {
    implicit val ec = executionContext
    implicit val req = request
    val b = (r: SecuredRequest[DefaultEnv, A]) => block(r).map(r => HandlerResult(r))

    requestHandler(request)(b).map(_.result).recoverWith(requestHandler.errorHandler.exceptionHandler)
  }
}
