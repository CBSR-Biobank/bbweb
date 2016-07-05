package org.biobank.domain

import org.biobank.fixture.NameGenerator

class AnnotationTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import AnnotationTypeSpecUtil._

  val nameGenerator = new NameGenerator(this.getClass)

  def createAnnotationType(tuple: AnnotTypeTuple): DomainValidation[AnnotationType] =
    (AnnotationType.create _).tupled(tuple)

  "An Annotation Type" can {

    "be created for each value type" in {
      AnnotationValueType.values.foreach { vt =>
        val tuple = AnnotationValueTypeToTuple(vt)
        createAnnotationType(tuple).mustSucceed { annotType =>
          annotType.uniqueId must not be empty
          annotType must have (
            'name          (tuple._1),
            'description   (tuple._2),
            'valueType     (tuple._3),
            'maxValueCount (tuple._4),
            'options       (tuple._5),
            'required      (tuple._6)
          )
        }
      }
    }

    "not be created with an null or empty name" in {
      val invalidNames: List[String] = List(null, "")

      invalidNames.foreach { invalidName =>
        createAnnotationType(numberAnnotationTypeTuple.copy(_1 = invalidName))
          .mustFail("NameRequired")
      }
    }

    "not be created with an empty description option" in {
      val invalidDescriptions = List(Some(null), Some(""))

      invalidDescriptions.foreach { invalidDescription =>
        createAnnotationType(numberAnnotationTypeTuple.copy(_2 = invalidDescription))
          .mustFail("InvalidDescription")
      }
    }

    "not be created with an negative max value count" in {
      createAnnotationType(numberAnnotationTypeTuple.copy(_4 = Some(-1)))
        .mustFail(1, "MaxValueCountError")
    }

    "not be created with an invalid options" in {
      createAnnotationType(numberAnnotationTypeTuple.copy(_5 = Seq("")))
        .mustFail(1, "OptionRequired")

      createAnnotationType(numberAnnotationTypeTuple.copy(_5 = Seq("dup", "dup")))
        .mustFail(1, "DuplicateOptionsError")
    }

    "have more than one validation fail" in {
      createAnnotationType(numberAnnotationTypeTuple.copy(
                             _1 = "",
                             _5 = Seq("dup", "dup")))
        .mustFail("NameRequired",
                  "DuplicateOptionsError",
                  "non select annotation type with options to select")
    }

  }
}
