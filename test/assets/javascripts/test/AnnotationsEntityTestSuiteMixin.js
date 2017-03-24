/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  AnnotationsEntityTestSuiteMixinFactory.$inject = [
    'AnnotationType',
    'AnnotationValueType',
    'TextAnnotation',
    'DateTimeAnnotation',
    'NumberAnnotation',
    'SingleSelectAnnotation',
    'MultipleSelectAnnotation',
    'factory'
  ];

  /**
   * A mixin for test suites for domain entities.
   */
  function AnnotationsEntityTestSuiteMixinFactory(AnnotationType,
                                                  AnnotationValueType,
                                                  TextAnnotation,
                                                  DateTimeAnnotation,
                                                  NumberAnnotation,
                                                  SingleSelectAnnotation,
                                                  MultipleSelectAnnotation,
                                                  factory) {

    function AnnotationsEntityTestSuiteMixin() {}

    AnnotationsEntityTestSuiteMixin.prototype.jsonAnnotationData = function () {
      var annotationTypes = factory.allAnnotationTypes();

      return _.map(annotationTypes, function (annotationType) {
        var value = factory.valueForAnnotation(annotationType);
        var annotation = factory.annotation(value, annotationType);

        return {
          annotationType: annotationType,
          annotation:     annotation
        };
      });
    };

    /**
     * @param {AnnotationType} annotationType the AnnotationType this annotion is based on.
     *
     * @param {Annotation} the annotation.
     */
    AnnotationsEntityTestSuiteMixin.prototype.validateAnnotationClass = function  (annotationType,
                                                                                   annotation) {
      switch (annotationType.valueType) {
      case AnnotationValueType.TEXT:
        expect(annotation).toEqual(jasmine.any(TextAnnotation));
        break;
      case AnnotationValueType.DATE_TIME:
        expect(annotation).toEqual(jasmine.any(DateTimeAnnotation));
        break;
      case AnnotationValueType.NUMBER:
        expect(annotation).toEqual(jasmine.any(NumberAnnotation));
        break;
      case AnnotationValueType.SELECT:
        if (annotationType.isSingleSelect()) {
          expect(annotation).toEqual(jasmine.any(SingleSelectAnnotation));
        } else {
          expect(annotation).toEqual(jasmine.any(MultipleSelectAnnotation));
        }
        break;

      default:
        fail('invalid annotation value type: ' + annotationType.valueType);
      }
    };

    return AnnotationsEntityTestSuiteMixin;
  }

  return AnnotationsEntityTestSuiteMixinFactory;
});
