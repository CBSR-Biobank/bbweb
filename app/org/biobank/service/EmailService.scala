package org.biobank.service

import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer._
import play.api.{ Configuration, Environment, Logger }
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class EmailService @Inject() (env:           Environment,
                              configuration: Configuration,
                              mailerClient:  MailerClient) {

  val log: Logger = Logger(this.getClass)

  def userRegisteredEmail(name: String, email: String): Unit = {
    val bodyHtml =s"""
|<html>
|<p>Administrator,</p>
|<p>A new user has registered with the Biobank server at ${serverUrl}. Please log in, verify the registration,
|and activate the new user.</p>
|<p>User's name: $name<br>
|Email address: $email</p>
|${emailFooter}
|</html>""".stripMargin

    sendEmail(subject  = "Biobank user registration",
              from     = adminEmail,
              to       = Seq(adminEmail),
              bodyHtml = bodyHtml)
  }

  def passwordResetEmail(recipient: String, password: String): Unit = {
    val bodyHtml = s"""
|<html>
|<p>The Biobank server at ${serverUrl} received a request to reset your password. Your new password is:</p>
|<pre>
|$password
|</pre>
|<p>The email address registered with the account is: $recipient.</p>
|<p>If you did not make this request, it is safe to disregard this message.</p>
|<p>Regards,<br/>Biobank</p>
|${emailFooter}
|</html>""".stripMargin

    sendEmail(subject  = "Biobank password reset",
              from     = adminEmail,
              to       = recipientEmails(recipient),
              bodyHtml = bodyHtml)
  }

  def userActivatedEmail(recipient: String): Unit = {
    val bodyHtml = s"""
|<html>
|<p>Your Biobank account has been activated. Please log into Biobank at:<br>
|${serverUrl}</p>
|<p>The email address registered with the account is: $recipient.</p>
|<p>Regards,<br/>Biobank</p>
|${emailFooter}
|</html>""".stripMargin

    sendEmail(subject  = "Biobank account activated",
              from     = adminEmail,
              to       = recipientEmails(recipient),
              bodyHtml = bodyHtml)
  }

  private def adminEmail: String =
    configuration.getString("admin.email").getOrElse("cbsrbiobank@gmail.com")

  private def serverUrl: String =
    configuration.getString("admin.url").getOrElse("biobank.com")

  private def recipientEmails(recipient: String): Seq[String] =
    if (env.mode == play.api.Mode.Prod) Seq[String](recipient, adminEmail)
    else Seq[String](adminEmail)


  @SuppressWarnings(Array("org.wartremover.warts.PublicInference"))
  private def sendEmail(subject:  String,
                        from:     String,
                        to:       Seq[String],
                        bodyHtml: String): Unit = {
    val async: Future[String] = Future {
        mailerClient.send(Email(subject, from, to, bodyHtml = Some(bodyHtml)))
      }

    async.onFailure {
      case err => Logger.error("mailer failed due to: " + err.getMessage)
    }
  }

  private def emailFooter: String =
      s"""
|<p><small>
This email was sent by the Biobank server at ${serverUrl}.
|</small></p>
|""".stripMargin

}
