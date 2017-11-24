package org.biobank.controllers.access

trait AccessControllerSpecCommon {

  protected def uri: String = "/api/access/"

  protected def uri(paths: String*): String = uri + paths.mkString("/")

}
