package org.biobank

import akka.stream.Materializer
import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject._
import org.biobank.domain.Slug
import org.biobank.domain.users._
import play.api.{Configuration, Logger}

/**
 * This is a trait so that it can be used by tests also.
 */
@Singleton
@SuppressWarnings(Array("org.wartremover.warts.Throw", "org.wartremover.warts.ImplicitParameter"))
class Global @Inject()(implicit val mat: Materializer,
                       configuration: Configuration) {

  val log: Logger = Logger(this.getClass)

  def checkConfig(): Unit = {
    if (configuration.get[String]("play.mailer.host").isEmpty) {
      throw new RuntimeException("smtp server information needs to be set in email.conf")
    }

    if (configuration.get[String]("admin.email").isEmpty) {
      throw new RuntimeException("administrator email needs to be set in application.conf")
    }

    val adminUrl = configuration.get[String]("admin.url")
    if (adminUrl.isEmpty) {
      throw new RuntimeException("administrator url needs to be set in application.conf")
    }
  }

  checkConfig
}

object Global {

  val DefaultUserEmail: String = "admin@admin.com"

  val DefaultUserId: UserId = UserId(Slug(DefaultUserEmail))

  val StartOfTime: OffsetDateTime = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

  val EndOfTime: OffsetDateTime = OffsetDateTime.of(9999, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

}
