/**
 * AngularJS Constants used for defining specimen types.
 *
 * @namespace domain.annotations.AnnotationMaxValueCount
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * Values that define an {@link domain.annotations.AnnotationType#maxValueCount maxValueCount}.
 *
 * @enum {int}
 * @memberOf domain.annotations.AnnotationMaxValueCount
 */
const AnnotationMaxValueCount = {
  /**
   * Used if {@link domain.annotations.AnnotationType#valueType valueType} for annotation type is not {@link
   * domain.AnnotationValueType.SELECT SELECT}
   *
   * @type {int}
   */
  NONE: 0,

  /**
   * Used when {@link domain.annotations.AnnotationType#valueType valueType} for annotation type is {@link
   * domain.AnnotationValueType.SELECT SELECT} and only a single value can be chosen.
   *
   * @type {int}
   */
  SELECT_SINGLE: 1,

  /**
   * Used when {@link domain.annotations.AnnotationType#valueType valueType} for annotation type is {@link
   * domain.AnnotationValueType.SELECT SELECT} and multiple values can be chosen.
   *
   * @type {int}
   */
  SELECT_MULTIPLE: 2
};

export default ngModule => ngModule.constant('AnnotationMaxValueCount', AnnotationMaxValueCount)
