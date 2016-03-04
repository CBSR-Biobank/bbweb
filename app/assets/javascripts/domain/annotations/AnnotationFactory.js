define(['underscore'], function(_) {
  'use strict';

  annotationFactoryFactory.$inject = [
    'funutils',
    'Annotation',
    'AnnotationValueType',
    'DateTimeAnnotation',
    'MultipleSelectAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'TextAnnotation'
  ];

  function annotationFactoryFactory(funutils,
                                    Annotation,
                                    AnnotationValueType,
                                    DateTimeAnnotation,
                                    MultipleSelectAnnotation,
                                    NumberAnnotation,
                                    SingleSelectAnnotation,
                                    TextAnnotation) {

    var service = { create: create };
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
      var valid, annotation;

      if (_.isUndefined(annotationType)) {
        throw new Error('annotation type is undefined');
      }

      if (obj) {
        valid = Annotation.isValid(obj);
        if (!valid) {
          throw new Error('invalid object from server: ' + Annotation.getInvalidError());
        }

        if (obj.selectedValues) {
          valid = annotationType.validOptions(obj.selectedValues);
        }

        if (!valid) {
          throw new Error('invalid selected values in object from server');
        }
      }

      switch (annotationType.valueType) {

      case AnnotationValueType.TEXT():
        annotation = new TextAnnotation(obj, annotationType);
        break;

      case AnnotationValueType.NUMBER():
        annotation = new NumberAnnotation(obj, annotationType);
        break;

      case AnnotationValueType.DATE_TIME():
        annotation = new DateTimeAnnotation(obj, annotationType);
        break;

      case AnnotationValueType.SELECT():
        if (annotationType.isSingleSelect()) {
          annotation = new SingleSelectAnnotation(obj, annotationType);
        } else if (annotationType.isMultipleSelect()) {
          annotation = new MultipleSelectAnnotation(obj, annotationType);
        } else {
          throw new Error('invalid select annotation: ' + annotationType.maxValueCount);
        }
        break;

      default:
        // should never happen since this is checked for in the constructor, but just in case
        throw new Error('value type is invalid: ' + annotationType.valueType);
      }

      return annotation;
    }
  }

  return annotationFactoryFactory;
});
