package org.biobank.domain

import play.api.libs.json._

class Slug(val id: String) extends AnyVal {
  override def toString: String = id
}

// this implementation borrowed from here:
// https://gist.github.com/sam/5213151
object Slug {

  def apply(id: String): Slug = new Slug(slugify(id))

  def slugify(input: String): String = {
    import java.text.Normalizer
    Normalizer.normalize(input, Normalizer.Form.NFD)
      .replaceAll("[^\\w\\s-]", "") // Remove all non-word, non-space or non-dash characters
      .replace('-', ' ')            // Replace dashes with spaces
      .trim                         // Trim leading/trailing whitespace (including what used to be
                                    // leading/trailing dashes)
      .replaceAll("\\s+", "-")      // Replace whitespace (including newlines and repetitions) with single
                                    // dashes
      .toLowerCase                  // Lowercase the final results
  }

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val slugFormat: Format[Slug] = new Format[Slug] {

      override def writes(id: Slug): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[Slug] =
        Reads.StringReads.reads(json).map(Slug.apply _)
    }

}
