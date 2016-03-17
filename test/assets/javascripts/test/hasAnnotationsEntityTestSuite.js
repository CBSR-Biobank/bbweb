/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['underscore'], function (_) {
  'use strict';

  hasAnnotationsEntityTestSuite.$inject = [
    'AnnotationType',
    'AnnotationValueType',
    'TextAnnotation',
    'DateTimeAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'MultipleSelectAnnotation',
    'jsonEntities'
  ];

  /**
   * A mixin for test suites for domain entities.
   */
  function hasAnnotationsEntityTestSuite(AnnotationType,
                                         AnnotationValueType,
                                         TextAnnotation,
                                         DateTimeAnnotation,
                                         NumberAnnotation,
                                         SingleSelectAnnotation,
                                         MultipleSelectAnnotation,
                                         jsonEntities) {
    var mixin = {
      jsonAnnotationData: jsonAnnotationData,
      validateAnnotationClass: validateAnnotationClass
    };

    return mixin;

    //--

    function jsonAnnotationData(serverStudy) {
      var annotationTypes = jsonEntities.allAnnotationTypes();

      return _.map(annotationTypes, function (annotationType) {
        var value = jsonEntities.valueForAnnotation(annotationType);
        var annotation = jsonEntities.annotation(value, annotationType);

        return {
          annotationType: annotationType,
          annotation:     annotation
        };
      });
    }

    /**
     * @param {AnnotationType} annotationType the AnnotationType this annotion is based on.
     *
     * @param {Annotation} the annotation.
     */
    function validateAnnotationClass(annotationType, annotation) {
      switch (annotationType.valueType) {
      case AnnotationValueType.TEXT():
        expect(annotation).toEqual(jasmine.any(TextAnnotation));
        break;
      case AnnotationValueType.DATE_TIME():
        expect(annotation).toEqual(jasmine.any(DateTimeAnnotation));
        break;
      case AnnotationValueType.NUMBER():
        expect(annotation).toEqual(jasmine.any(NumberAnnotation));
        break;
      case AnnotationValueType.SELECT():
        if (annotationType.isSingleSelect()) {
          expect(annotation).toEqual(jasmine.any(SingleSelectAnnotation));
        } else {
          expect(annotation).toEqual(jasmine.any(MultipleSelectAnnotation));
        }
        break;

      default:
        fail('invalid annotation value type: ' + annotationType.valueType);
      }
    }

  }

  return hasAnnotationsEntityTestSuite;

});
