package org.biobank.service

import com.typesafe.plugin._
import play.api.Play.current
import play.Play
import play.api.Logger
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

object EmailService {

  def passwordResetEmail(recipient: String, password: String) = {
    val async: Future[Unit] = Future {
      val adminEmail = Play.application.configuration.getString("admin.email")

      val mail = use[MailerPlugin].email
      mail.setSubject("Biobank password reset")
      mail.setFrom(adminEmail)

      if (Play.isProd) {
        mail.setRecipient(recipient)
      } else {
        mail.setRecipient(adminEmail)
      }

      val msg = s"""|<html>
                    |<p>We received a request to reset your password. Your new password is:</p>
                    |<pre>
                    |$password
                    |</pre>
                    |<p>Email address for account is: $recipient</p>
                    |<p>If you did not make this request it is safe to disregard this message.</p>
                    |<p>Regards,<br/>Biobank</p>
                    |</html>""".stripMargin
      mail.sendHtml(msg)
    }

    async.onFailure { case(err) =>
      Logger.error("mailer failed due to: "+ err.getMessage)
    }
  }

}
