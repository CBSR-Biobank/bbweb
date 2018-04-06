/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';

/**
 * A mixin for test suites for {@link domain.annotations.Annotation Annotations}.
 *
 * @exports test.mixins.AnnotationsEntityTestSuiteMixin
 */
let AnnotationsEntityTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([
      'EntityTestSuiteMixin',
      'AnnotationType',
      'AnnotationValueType',
      'AnnotationMaxValueCount',
      'TextAnnotation',
      'DateTimeAnnotation',
      'NumberAnnotation',
      'SingleSelectAnnotation',
      'MultipleSelectAnnotation',
      'annotationFactory',
      'Factory'
    ]);
    EntityTestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  /**
   * Creates Annotations Types and Annotations for each value type.

   * @return {Array<object>} an array of objects containing: an {@link domain.annotations.AnnotationType
   * AnnotationType}, and a corresponding {@link domain.annotations.Annotation Annotation}.
   */
  jsonAnnotationData: function () {
    var annotationTypes = this.Factory.allAnnotationTypes();

    return annotationTypes.map((annotationType) => {
      var value = this.Factory.valueForAnnotation(annotationType);
      var annotation = this.Factory.annotation(value, annotationType);

      return {
        annotationType: annotationType,
        annotation:     annotation
      };
    });
  },

  /**
   * @param {object} annotTypeOptions - The options passed to the {@link test.Factory Factory} when creating
   * the *Annotation Type*.
   *
   * @return {object} an object containing: an {@link domain.annotations.AnnotationType AnnotationType}, an
   * annotation as a plain object, and an {@link domain.annotations.Annotation Annotation}.
   */
  getAnnotationAndType: function (annotTypeOptions = {}) {
    const annotationType   = this.AnnotationType.create(this.Factory.annotationType(annotTypeOptions)),
          value            = this.Factory.valueForAnnotation(annotationType),
          jsonAnnotation   = this.Factory.annotation({ value }, annotationType),
          annotation       = this.annotationFactory.create(jsonAnnotation, annotationType);

    return {
      annotationType,
      jsonAnnotation,
      annotation
    };
  },

  /*
   * Creates *Annotation Type* options to create an annotation of each type of object.
   *
   * @see test.Factory.annotationType
   */
  annotationTypesForAllValueTypes: function () {
    const result = Object.values(this.AnnotationValueType).map((valueType) => ({ valueType: valueType }));
    result.push({
      valueType:     this.AnnotationValueType.SELECT,
      maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
      options:       [ 'opt1', 'opt2', 'opt3' ]
    });
    return result;
  },

  /**
   * A function that tests the type of an Annotation object.
   *
   * @param {AnnotationType} annotationType the AnnotationType this annotion is based on.
   *
   * @param {Annotation} the annotation.
   */
  validateAnnotationClass: function (annotationType, annotation) {
    switch (annotationType.valueType) {
    case this.AnnotationValueType.TEXT:
      expect(annotation).toEqual(jasmine.any(this.TextAnnotation));
      break;
    case this.AnnotationValueType.DATE_TIME:
      expect(annotation).toEqual(jasmine.any(this.DateTimeAnnotation));
      break;
    case this.AnnotationValueType.NUMBER:
      expect(annotation).toEqual(jasmine.any(this.NumberAnnotation));
      break;
    case this.AnnotationValueType.SELECT:
      if (annotationType.isSingleSelect()) {
        expect(annotation).toEqual(jasmine.any(this.SingleSelectAnnotation));
      } else {
        expect(annotation).toEqual(jasmine.any(this.MultipleSelectAnnotation));
      }
      break;

    default:
      fail('invalid annotation value type: ' + annotationType.valueType);
    }
  }

}

AnnotationsEntityTestSuiteMixin = Object.assign({}, EntityTestSuiteMixin, AnnotationsEntityTestSuiteMixin);

export { AnnotationsEntityTestSuiteMixin };
export default () => {};
