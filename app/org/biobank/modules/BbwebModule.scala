package org.biobank.modules

import com.google.inject.AbstractModule
import org.biobank.Global

class BbwebModule extends AbstractModule {

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def configure() = {
    bind(classOf[Global]).asEagerSingleton()
  }

}
