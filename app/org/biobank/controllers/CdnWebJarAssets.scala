package org.biobank.controllers

import play.api.Play
import play.api.Play.current

/**
 * See http://www.jamesward.com/2014/03/20/webjars-now-on-the-jsdelivr-cdn
 */
object CdnWebJarAssets extends controllers.WebJarAssets(controllers.Assets) {

  def getUrl(file: String) = {
    val maybeContentUrl = Play.configuration.getString("contentUrl")

    maybeContentUrl.map { contentUrl =>
        contentUrl + org.biobank.controllers.routes.CdnWebJarAssets.at(file).url
    } getOrElse org.biobank.controllers.routes.CdnWebJarAssets.at(file).url
  }

}
