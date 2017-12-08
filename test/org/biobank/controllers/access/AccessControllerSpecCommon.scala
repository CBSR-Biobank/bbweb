package org.biobank.controllers.access

trait AccessControllerSpecCommon {

  protected def uri(paths: String*): String = {
    val basePath = "/api/access"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

}
