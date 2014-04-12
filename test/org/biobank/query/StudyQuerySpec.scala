package org.biobank.query

import org.biobank.domain.UserId
import org.biobank.query.model._
import fixture._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.Messages._

import java.util.UUID
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.scalatest.junit.JUnitRunner
import play.api.test.FakeApplication
import play.api.test.Helpers._
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.session.Session

@RunWith(classOf[JUnitRunner])
class StudyQuerySpec {

  //  val log = LoggerFactory.getLogger(this.getClass)
  //
  //  val nameGenerator = new NameGenerator(classOf[StudyQuerySpec].getName)
  //
  //  "Study" can {
  //
  //    "be added" in {
  //      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
  //        val id = UUID.randomUUID.toString
  //        val version = 0L
  //        val name = nameGenerator.next[String]
  //        val description = Some(nameGenerator.next[String])
  //        studyView ! StudyAddedEvent(id, version, name, description)
  //
  //        implicit val getStudyResult = GetResult(r => Study(r.<<, r.<<, r.<<, r.<<, r.<<))
  //
  //        val r = DB.withSession { implicit s: Session =>
  //          val DDL = Studies.Studies.ddl.createStatements
  //
  //          Q.queryNA[Study]("select * from study") foreach { c =>
  //            c.name mustEqual name
  //          }
  //        }
  //      }
  //      true
  //    }
  //  }

}
