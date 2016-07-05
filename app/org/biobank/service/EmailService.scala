package org.biobank.service

import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer._
import play.api.{ Configuration, Environment, Logger }
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

@Singleton
class EmailService @Inject() (env: Environment,
                              configuration: Configuration,
                              mailerClient: MailerClient) {

  def passwordResetEmail(recipient: String, password: String) = {

    @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
    val async: Future[Unit] = Future {
        val adminEmail = configuration.getString("admin.email").getOrElse("cbsrbiobank@gmail.com")
      val to = if (env.mode == play.api.Mode.Prod) {
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
