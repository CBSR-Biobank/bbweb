package service

import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps

trait ApplicationService {

  implicit val timeout = Timeout(5 seconds)

}