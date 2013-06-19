package controllers

import securesocial.controllers.TemplatesPlugin
import securesocial.core.{ Identity, SecuredRequest, SocialUser }
import securesocial.core.SocialUser
import play.api.templates.{ Html, Txt }
import play.api.mvc.{ RequestHeader, Request }
import play.api.data.Form
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.Registration.RegistrationInfo

/**
 * Overrides the default SecureSocial views.
 */
class SecureSocialViews(application: play.api.Application) extends TemplatesPlugin {
  /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
    msg: Option[String] = None): Html = {
    views.html.custom.login(form, msg)
  }

  /**
   * Returns the html for the signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.custom.Registration.signUp(form, token)
  }

  /**
   * Returns the html for the start signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.custom.Registration.startSignUp(form)
  }

  /**
   * Returns the html for the reset password page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.custom.Registration.startResetPassword(form)
  }

  /**
   * Returns the html for the start reset page
   *
   * @param request
   * @tparam A
   * @return
   */
  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.custom.Registration.resetPasswordPage(form, token)
  }

  /**
   * Returns the html for the change password page
   *
   * @param request
   * @param form
   * @tparam A
   * @return
   */
  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]): Html = {
    views.html.custom.passwordChange(form)
  }

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.custom.notAuthorized()
  }

  /**
   * Returns the email sent when a user starts the sign up process
   *
   * @param token the token used to identify the request
   * @param request the current http request
   * @return a String with the html code for the email
   */
  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.custom.mails.signUpEmail(token)))
  }

  /**
   * Returns the email sent when the user is already registered
   *
   * @param user the user
   * @param request the current request
   * @return a String with the html code for the email
   */
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
    (None, Some(views.html.custom.mails.unknownEmailNotice(request)))
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