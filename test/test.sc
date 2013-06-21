import scalaz._
import scalaz.Scalaz._

object test {
  val cmd = UpdateStudyCmd("a",1)
  cmd.studyId
}

trait Command {}

trait Identity { val id: String }

trait ExpectedVersion { val expectedVersion: Option[Long] }

trait StudyCommand extends Command with Identity with ExpectedVersion {
  val studyId: String
}
case class UpdateStudyCmd(id: String, expectedVersion: Option[String],
studyId: String, version: Long) extends StudyCommand