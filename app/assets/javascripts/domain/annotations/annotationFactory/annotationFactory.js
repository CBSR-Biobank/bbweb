/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/* @ngInject */
function AnnotationFactory(Annotation,
                           AnnotationValueType,
                           DateTimeAnnotation,
                           MultipleSelectAnnotation,
                           NumberAnnotation,
                           SingleSelectAnnotation,
                           TextAnnotation,
                           DomainError) {

  var service = {
    create
  };
  return service;

  //--

  /**
   * These objects are used by HTML form code to manage annotation information. It differs from the
   * server representation in order to make setting the information via an HTML simpler.
   *
   * This static method should be used instead of the constructor when creating an annotation from a server
   * response, since it validates that the required fields are present.
   *
   * @param {Object} obj - the server side entity
   *
   * @param {AnnotationType} annotationType the annotation type this annotation is based from
   *
   * @param {boolean} required set only if annotationType does not have a 'required' attribute.
   */
  function create(obj, annotationType) {
    var validation, annotation;

    if (_.isUndefined(annotationType)) {
      throw new DomainError('annotation type is undefined: ' + obj.annotationTypeId);
    }

    if (obj) {
      validation = Annotation.isValid(obj);
      if (!validation.valid) {
        throw new DomainError('invalid annotation from server: ' + validation.error);
      }

      if (obj.selectedValues) {
        validation.valid = annotationType.validOptions(obj.selectedValues);
      }

      if (!validation.valid) {
        throw new DomainError('invalid selected values in object from server');
      }
    }

    switch (annotationType.valueType) {

    case AnnotationValueType.TEXT:
      annotation = new TextAnnotation(obj, annotationType);
      break;

    case AnnotationValueType.NUMBER:
      annotation = new NumberAnnotation(obj, annotationType);
      break;

    case AnnotationValueType.DATE_TIME:
      annotation = new DateTimeAnnotation(obj, annotationType);
      break;

    case AnnotationValueType.SELECT:
      if (annotationType.isSingleSelect()) {
        annotation = new SingleSelectAnnotation(obj, annotationType);
      } else if (annotationType.isMultipleSelect()) {
        annotation = new MultipleSelectAnnotation(obj, annotationType);
      } else {
        throw new DomainError('invalid select annotation: ' + annotationType.maxValueCount);
      }
      break;

    default:
      // should never happen since this is checked for in the constructor, but just in case
      throw new DomainError('value type is invalid: ' + annotationType.valueType);
    }

    return annotation;
  }

}

export default ngModule => ngModule.service('annotationFactory', AnnotationFactory)
