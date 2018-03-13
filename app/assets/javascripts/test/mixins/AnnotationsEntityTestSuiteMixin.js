/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * A mixin for test suites for annotation domain entities.
 */
/* @ngInject */
function AnnotationsEntityTestSuiteMixin(EntityTestSuiteMixin,
                                         AnnotationType,
                                         AnnotationValueType,
                                         TextAnnotation,
                                         DateTimeAnnotation,
                                         NumberAnnotation,
                                         SingleSelectAnnotation,
                                         MultipleSelectAnnotation,
                                         Factory) {

  return Object.assign({ jsonAnnotationData, validateAnnotationClass }, EntityTestSuiteMixin);

  function jsonAnnotationData() {
    var annotationTypes = Factory.allAnnotationTypes();

    return annotationTypes.map((annotationType) => {
      var value = Factory.valueForAnnotation(annotationType);
      var annotation = Factory.annotation(value, annotationType);

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
  }

}

export default ngModule => ngModule.service('AnnotationsEntityTestSuiteMixin', AnnotationsEntityTestSuiteMixin)
