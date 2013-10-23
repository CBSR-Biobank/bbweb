package query

import query.model._
import fixture._
import service.events._

import org.eligosource.eventsourced.core.Message
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import scala.slick.session.Session
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import org.slf4j.LoggerFactory
import play.api.test.FakeApplication
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class StudyQuerySpec extends StudyQueryFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(classOf[StudyQuerySpec].getName)

  "Study" can {

    "be added" in {
      val id = nameGenerator.next[String]
      val version = 0L
      val name = nameGenerator.next[String]
      val description = Some(nameGenerator.next[String])
      studyEventProcessor ! Message(StudyAddedEvent(id, version, name, description))

      implicit val getStudyResult = GetResult(r => Study(r.<<, r.<<, r.<<, r.<<, r.<<))

      val r = DB.withSession { implicit s: Session =>
        val DDL = Studies.Studies.ddl.createStatements

        Q.queryNA[Study]("select * from study") foreach { c =>
          c.name mustEqual name
        }
      }
      true
    }
  }

}