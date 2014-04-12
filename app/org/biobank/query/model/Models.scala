package org.biobank.query.model

import java.util.Date
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import slick.lifted.{ Join, MappedTypeMapper }

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

/**
 * Data Access Object trait
 *
 *  Used to create the DAOs:
 */
private[model] trait DAO {
  val Studies = new Studies
}

case class Study(id: String, version: Long, name: String, description: Option[String], enabled: Boolean)

class Studies extends Table[Study]("STUDY") {
  def id = column[String]("id", O.PrimaryKey)
  def version = column[Long]("version", O.NotNull)
  def name = column[String]("name", O.NotNull)
  def description = column[Option[String]]("description", O.Nullable, O.DBType("text"))
  def enabled = column[Boolean]("enabled", O.NotNull, O.DBType("bit(1)"))
  def * = id ~ version ~ name ~ description ~ enabled <> (Study.apply _, Study.unapply _)

  val byId = createFinderBy(_.id)
}

object Studies extends DAO {
  /**
   * Construct the Map[String,String] needed to fill a select options set
   */
  def options(implicit s: Session): Seq[(String, String)] = {
    val query = (for {
      study <- Studies
    } yield (study.id, study.name, study.description, study.enabled)).sortBy(_._2)
    query.list.map(row => (row._1.toString, row._2))
  }

  /**
   * Insert a new study
   */
  def insert(study: Study)(implicit s: Session) {
    Studies.insert(study)
  }

  def update(study: Study)(implicit s: Session) {
    val studyToUpdate: Study = study.copy(study.id)
    Studies.where(_.id === study.id).update(studyToUpdate)
  }
}