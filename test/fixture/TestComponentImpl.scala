package fixture

import service._
import domain._

import play.api.Mode
import play.api.Mode._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import akka.actor.ActorSystem
import akka.actor.Props
import akka.util.Timeout
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.slf4j.LoggerFactory
import akka.actor._

import scalaz._
import scalaz.Scalaz._

trait TestComponentImpl extends TopComponent with ServiceComponentImpl {

  protected val nameGenerator: NameGenerator

  implicit val system = ActorSystem("bbweb-test")
  implicit val timeout = Timeout(5 seconds)

  protected implicit val adminUserId = new UserId("admin@admin.com")

}
