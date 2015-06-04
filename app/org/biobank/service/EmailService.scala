package org.biobank.service

import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer._
import play.api.Play.current
import play.Play
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class EmailService @Inject() (mailerClient: MailerClient) {

  def passwordResetEmail(recipient: String, password: String) = {
    val async: Future[Unit] = Future {
      val adminEmail = Play.application.configuration.getString("admin.email")
      val to = if (Play.isProd) {
        recipient
      } else {
        adminEmail
      }

      val bodyHtml = s"""|<html>
                         |<p>We received a request to reset your password. Your new password is:</p>
                         |<pre>
                         |$password
                         |</pre>
                         |<p>Email address for account is: $recipient</p>
                         |<p>If you did not make this request, it is safe to disregard this message.</p>
                         |<p>Regards,<br/>Biobank</p>
                         |</html>""".stripMargin

      val email = Email(subject  = "Biobank password reset",
                        from     = adminEmail,
                        to       = Seq(to),
                        bodyHtml = Some(bodyHtml))

      mailerClient.send(email)
      ()
    }

    async.onFailure { case(err) =>
      Logger.error("mailer failed due to: "+ err.getMessage)
    }
  }

}
