package query

import domain.UserId
import query.model._
import fixture._
import service.events.StudyEvents._
import service.Messages._

import java.util.UUID
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.specs2.runner.JUnitRunner
import play.api.test.FakeApplication
import play.api.test.Helpers._
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.session.Session

@RunWith(classOf[JUnitRunner])
class StudyQuerySpec extends StudyQueryFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(classOf[StudyQuerySpec].getName)

  "Study" can {

    "be added" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val id = UUID.randomUUID.toString
        val version = 0L
        val name = nameGenerator.next[String]
        val description = Some(nameGenerator.next[String])
        studyView ! StudyAddedEvent(id, version, name, description)

        implicit val getStudyResult = GetResult(r => Study(r.<<, r.<<, r.<<, r.<<, r.<<))

        val r = DB.withSession { implicit s: Session =>
          val DDL = Studies.Studies.ddl.createStatements

          Q.queryNA[Study]("select * from study") foreach { c =>
            c.name mustEqual name
          }
        }
      }
      true
    }
  }

}