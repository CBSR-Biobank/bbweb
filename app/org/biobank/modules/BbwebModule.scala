package org.biobank.modules

import com.google.inject.AbstractModule
import org.biobank.{Global, TestData}

class BbwebModule extends AbstractModule {

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  override def configure() = {
    bind(classOf[Global]).asEagerSingleton
    bind(classOf[TestData]).asEagerSingleton
  }

}
