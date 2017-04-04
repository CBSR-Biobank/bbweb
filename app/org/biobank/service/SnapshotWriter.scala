package org.biobank.service

import com.google.inject.ImplementedBy
import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}
import javax.inject.Inject
import org.joda.time.LocalDateTime
import play.api.{Environment, Mode}

@ImplementedBy(classOf[SnapshotWriterImpl])
trait SnapshotWriter {

  def save(basename: String, content: String): String

  def load(filename: String): String
}

/**
 * Writes an Akka Persistence snapshot.
 *
 */
class SnapshotWriterImpl @Inject() (val env: Environment) extends SnapshotWriter {

  /**
   * When running in Test mode, the snapshots are written to a different path.
   */
  def save(basename: String, content: String): String = {
    val dateTime = new LocalDateTime();
    val path = if (env.mode == Mode.Test) "/tmp/bbweb/test"
               else "/tmp/bbweb"
    val filename = s"${path}/${basename}_snapshot_${dateTime.toString}.json.gz"
    val fos = new FileOutputStream(filename)
    val gzos = new GZIPOutputStream(fos)
    val w    = new PrintWriter(gzos)

    w.write(content)
    w.close();
    filename
  }

  def load(filename: String): String = {
    val in = new GZIPInputStream(new FileInputStream(filename))
    scala.io.Source.fromInputStream(in).getLines.mkString
  }

}
