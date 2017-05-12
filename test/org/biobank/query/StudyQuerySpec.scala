package org.biobank.query

// import org.slf4j.LoggerFactory
import org.scalatest._
//import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
//import scala.slick.session.Session

class StudyQuerySp extends WordSpecLike with MustMatchers {

  //  val log = LoggerFactory.getLogger(this.getClass)
  //
  //  vaameGenerator = new NameGenerator[this.getClass]
  //
  //  describe("Study") {
  //
  //    it("be added") {
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
