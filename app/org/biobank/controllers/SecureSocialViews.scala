package org.biobank.controllers

import securesocial.controllers.TemplatesPlugin
import securesocial.core.{ Identity, SecuredRequest, SocialUser }
import securesocial.core.SocialUser
import play.api.templates.{ Html, Txt }
import play.api.mvc.{AnyContent, Controller, RequestHeader, Request}
import play.api.data.Form
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo

/**
  * Overrides the default SecureSocial views.
  *
  *  method defs come from securesocial.controllers.DefaultTemplatesPlugin
  */
class SecureSocialViews(application: play.api.Application) extends TemplatesPlugin {
  override def getLoginPage(
    form: Form[(String, String)],
    msg: Option[String] = None)(implicit request: Request[AnyContent]): Html = {
    views.html.custom.login(form, msg)
  }

  override def getSignUpPage(
    form: Form[RegistrationInfo],
    token: String)(implicit request: Request[AnyContent]): Html = {
    views.html.custom.Registration.signUp(form, token)
  }

  override def getStartSignUpPage(form: Form[String])(implicit request: Request[AnyContent]): Html = {
    views.html.custom.Registration.startSignUp(form)
  }

  override def getStartResetPasswordPage(form: Form[String])(implicit request: Request[AnyContent]): Html = {
    views.html.custom.Registration.startResetPassword(form)
  }

  override def getResetPasswordPage(
    form: Form[(String, String)],
    token: String)(implicit request: Request[AnyContent]): Html = {
    views.html.custom.Registration.resetPasswordPage(form, token)
  }

  override def getPasswordChangePage(form: Form[ChangeInfo])(implicit request: SecuredRequest[AnyContent]):Html = {
    views.html.custom.passwordChange(form)
  }

  def getNotAuthorizedPage(implicit request: Request[AnyContent]): Html = {
    views.html.custom.notAuthorized()
  }

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.signUpEmail(token)))
  }

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.alreadyRegisteredEmail(user)))
  }

  /**
   * Returns the welcome email sent when the user finished the sign up process
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.welcomeEmail(user)))
  }
  /**
   * Returns the email sent when a user tries to reset the password but there is no account for
   * that email address in the system
   *
   * @param request the current request
   * @return a String with the html code for the email
   */
  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.unknownEmailNotice()))
  }

  /**
   * Returns the email sent to the user to reset the password
   *
   * @param user the user
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.passwordResetEmail(user, token)))
  }
  /**
   * Returns the email sent as a confirmation of a password change
   *
   * @param user the user
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.passwordChangedNotice(user)))
  }
}
