package org.biobank.services

import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer._
import play.api.{ Configuration, Environment, Logger }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
@Singleton
class EmailService @Inject() (env:           Environment,
                              configuration: Configuration,
                              mailerClient:  MailerClient)
                          (implicit ec: ExecutionContext){

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

  private def adminEmail: String = configuration.get[String]("admin.email")

  private def serverUrl: String =
    configuration.get[Option[String]]("admin.url").getOrElse("biobank.com")

  private def recipientEmails(recipient: String): Seq[String] =
    if (env.mode == play.api.Mode.Prod) Seq[String](recipient, adminEmail)
    else Seq[String](adminEmail)


  private def sendEmail(subject:  String,
                        from:     String,
                        to:       Seq[String],
                        bodyHtml: String): Unit = {
    val async: Future[String] = Future {
        mailerClient.send(Email(subject, from, to, bodyHtml = Some(bodyHtml)))
      }

    async.onComplete {
      case Failure(err) => Logger.error("mailer failed due to: " + err.getMessage)
      case Success(_) =>
    }
  }

  private def emailFooter: String =
      s"""
|<p><small>
This email was sent by the Biobank server at ${serverUrl}.
|</small></p>
|""".stripMargin

}
