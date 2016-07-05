package org.biobank.domain

import org.biobank._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Annotation types define an [[Annotation]].
 *
 * Annotations allow entities to collect custom named and defined pieces of data.
 *
 * This is a value object.
 *
 * @param valueType The type of information stored by the annotation. I.e. text, number, date, or an
 * item from a drop down list. See [[AnnotationValueType]].
 *
 * @param maxValueCount When valueType is [[AnnotationValueType.Select]] (i.e. a drop down list),
 * this is the number of items allowed to be selected. If the value is 0 then any number of values can be
 * selected.
 *
 * @param options When valueType is [[AnnotationValueType.Select]], these are the list of options allowed to
 * be selected.
 *
 * @param required When true, the user must enter a value for this annotation.
 */
final case class AnnotationType(uniqueId:      String,
                                name:          String,
                                description:   Option[String],
                                valueType:     AnnotationValueType,
                                maxValueCount: Option[Int],
                                options:       Seq[String],
                                required:      Boolean)
    extends HasName
    with HasDescriptionOption
    with AnnotationTypeValidations {

  override def equals(that: Any): Boolean = {
    that match {
      case that: AnnotationType => this.uniqueId.equalsIgnoreCase(that.uniqueId)
      case _ => false
    }
  }

  override def hashCode:Int = {
    uniqueId.toUpperCase.hashCode
  }

  override def toString: String =
    s"""|AnnotationType:{
        |  uniqueId:              $uniqueId,
        |  name:                  $name,
        |  description:           $description,
        |  valueType:             $valueType,
        |  maxValueCount:         $maxValueCount,
        |  options:               { $options },
        |  required:              $required
        |}""".stripMargin

}

trait AnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  case object MaxValueCountError extends ValidationKey

  case object OptionRequired extends ValidationKey

  case object DuplicateOptionsError extends ValidationKey

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(name:          String,
               description:   Option[String],
               valueType:     AnnotationValueType,
               maxValueCount: Option[Int],
               options:       Seq[String],
               required:      Boolean)
      : DomainValidation[Boolean] = {
    (validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       validateMaxValueCount(maxValueCount) |@|
       validateOptions(options) |@|
       validateSelectParams(valueType, maxValueCount, options)) {
      case (_, _, _, _, _) => true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(annotationType: AnnotationType): DomainValidation[Boolean] = {
    validate(annotationType.name,
             annotationType.description,
             annotationType.valueType,
             annotationType.maxValueCount,
             annotationType.options,
             annotationType.required)
  }

  def validateMaxValueCount(option: Option[Int]): DomainValidation[Option[Int]] = {
    option.fold {
      none[Int].successNel[String]
    } { n  =>
      if (n > -1) {
        option.successNel
      } else {
        MaxValueCountError.failureNel[Option[Int]]
      }
    }
  }

  /**
   *  Validates each item in the map and returns all failures.
   */
  def validateOptions(options: Seq[String]): DomainValidation[Seq[String]] = {
    if (options.distinct.size === options.size) {
      options.toList.map(validateString(_, OptionRequired)).sequenceU.fold(
        err => err.toList.mkString(",").failureNel[Seq[String]],
        list => list.toSeq.successNel
      )
    } else {
      DuplicateOptionsError.failureNel[Seq[String]]
    }
  }

  /** If an annotation type is for a select, the following is required:
   *
   * - max value count must be 1 or 2
   * - options must be a non-empty sequence
   *
   * If an annotation type is not for a select, the following is required
   *
   * - max value count must be 0
   * - options must be None
   */
  def validateSelectParams(valueType:     AnnotationValueType,
                           maxValueCount: Option[Int],
                           options:       Seq[String])
      : DomainValidation[Boolean] = {
    if (valueType == AnnotationValueType.Select) {
      maxValueCount.fold {
        DomainError(s"max value count is invalid for select").failureNel[Boolean]
      } { count =>
        val countValidation = if ((count < 1) || (count > 2)) {
            DomainError(s"select annotation type with invalid maxValueCount: $count").failureNel[Boolean]
          } else {
            true.successNel[String]
          }

        val optionsValidation = if (options.isEmpty) {
            DomainError("select annotation type with no options to select").failureNel[Boolean]
          } else {
            true.successNel[String]
          }

        (countValidation |@| optionsValidation) {
          case(_, _) => true
        }
      }
    } else {
      val countValidation = maxValueCount.fold {
          true.successNel[String]
        } { count =>
          DomainError(s"max value count is invalid for non-select").failureNel[Boolean]
        }

      val optionsValidation = if (options.isEmpty) {
          true.successNel[String]
        } else {
          DomainError("non select annotation type with options to select").failureNel[Boolean]
        }

        (countValidation |@| optionsValidation) {
          case(_, _) => true
        }
    }
  }

}

object AnnotationType extends AnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  implicit val annotationTypeWrites: Writes[AnnotationType] = Json.writes[AnnotationType]

  def create(name:          String,
             description:   Option[String],
             valueType:     AnnotationValueType,
             maxValueCount: Option[Int],
             options:       Seq[String],
             required:      Boolean) = {
    (validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       validateMaxValueCount(maxValueCount) |@|
       validateOptions(options) |@|
       validateSelectParams(valueType, maxValueCount, options)) {
      case (_, _, _, _, _) =>
        val uniqueId = java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase
        AnnotationType(uniqueId, name, description, valueType, maxValueCount, options, required)
    }
  }

}
