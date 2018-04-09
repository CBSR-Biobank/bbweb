package org.biobank.controllers

import org.biobank.domain.Slug
import play.api.mvc.PathBindable.Parsing
import play.api.routing.sird._

object SlugRouting {

  implicit object bindableSlug extends Parsing[Slug](
    Slug.apply,
    _.id,
    (key: String, e: Exception) => s"$key is not a valid slug"
  )

  val slug: PathBindableExtractor[Slug]   = new PathBindableExtractor[Slug]

}
