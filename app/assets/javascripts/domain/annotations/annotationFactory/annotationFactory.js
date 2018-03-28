/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * An AngularJS service for {@link domain.Annotation Annotations}.
 *
 * @memberOf domain
 */
class AnnotationFactory {

  constructor(Annotation,
              AnnotationValueType,
              DateTimeAnnotation,
              MultipleSelectAnnotation,
              NumberAnnotation,
              SingleSelectAnnotation,
              TextAnnotation,
              DomainError) {
    'ngInject';
    Object.assign(this,
                  {
                    Annotation,
                    AnnotationValueType,
                    DateTimeAnnotation,
                    MultipleSelectAnnotation,
                    NumberAnnotation,
                    SingleSelectAnnotation,
                    TextAnnotation,
                    DomainError
                  });
  }

  /**
   * A factory function for {@link domain.Annotation Annotations}.
   *
   * Creates an {@link domain.Annotation Annotations} from a reply from the server.
   *
   * @param {Object} obj={} - the JSON response for an Annotation from the server.
   *
   * @param {domain.AnnotationType} annotationType - the annotation type this annotation is based from
   */
  create(obj = {}, annotationType) {
    let annotation;

    if (_.isUndefined(annotationType)) {
      throw new this.DomainError('annotation type is undefined: ' + obj.annotationTypeId);
    }

    switch (annotationType.valueType) {

    case this.AnnotationValueType.TEXT:
      annotation = this.TextAnnotation.create(obj, annotationType);
      break;

    case this.AnnotationValueType.NUMBER:
      annotation = this.NumberAnnotation.create(obj, annotationType);
      break;

    case this.AnnotationValueType.DATE_TIME:
      annotation = this.DateTimeAnnotation.create(obj, annotationType);
      break;

    case this.AnnotationValueType.SELECT:
      if (annotationType.isSingleSelect()) {
        annotation = this.SingleSelectAnnotation.create(obj, annotationType);
      } else if (annotationType.isMultipleSelect()) {
        annotation = this.MultipleSelectAnnotation.create(obj, annotationType);
      } else {
        throw new this.DomainError('invalid select annotation: ' + annotationType.maxValueCount);
      }
      break;

    default:
      // should never happen since this is checked for in the create method, but just in case
      throw new this.DomainError('value type is invalid: ' + annotationType.valueType);
    }

    return annotation;
  }

}

export default ngModule => ngModule.service('annotationFactory', AnnotationFactory)
