package org.biobank.service

import org.biobank.fixture._

class SpecimenLinkTypeProcessorSpec extends StudyProcessorFixture {

  "A study processor" can {
    "add a collection event type" in {
    }

    "not add a collection event type with a name that already exists" in {
    }

    "update a collection event type" in {
    }

    "not update a collection event type to name that already exists" in {
    }

    "not update a collection event type to wrong study" in {
    }

    "not update a collection event type with an invalid version" in {
    }

    "remove a collection event type" in {
    }

    "not remove a collection event type  with an invalid version" in {
    }

    "add a specimen group to a collection event type" in {
    }

    "update a collection event type and add specimen groups" in {
    }

    "not update a specimen group if it used by collection event type" in {
    }

    "remove a specimen group from collection event type" in {
    }

    "not remove a specimen group if used by collection event type" in {
    }

    "not add a specimen group from a different study" in {
    }

    "not update a collection event type with a specimen group from a different study" in {
    }

    "add an annotation type to a collection event" in {
    }

    "not update an annotation type if used by specimen link type" in {
    }

    "remove an annotation type from specimen link type" in {
    }

    "not remove an annotation type if it is used by specimen link type" in {
    }

    "not add an annotation type if it is in wrong study" in {

    }
  }
}

