/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import annotationSharedBehaviour from 'test/behaviours/annotationSharedBehaviour';
import ngModule from '../../index'

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('NumberAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, EntityTestSuiteMixin);
      this.injectDependencies('NumberAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'Factory');

      this.annotationTypeJson = (options = {}) => {
        const opts = Object.assign(options, { valueType: this.AnnotationValueType.NUMBER });
        return this.Factory.annotationType(opts);
      };
    });
  });

  it('getValue returns valid results', function() {
    const annotationType = this.AnnotationType.create(this.annotationTypeJson());
    const value = this.Factory.valueForAnnotation(annotationType);
    const annotationJson = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.NumberAnnotation.create(annotationJson, annotationType);

    expect(annotation.getValue()).toEqual(parseFloat(annotationJson.numberValue));
  });

  it('isValueValue returns valid results for a required annotation', function() {
    const annotationType = this.AnnotationType.create(this.annotationTypeJson({ required: true }));
    const expectations = [
      { value: '', expected: false }
    ];

    expectations.forEach((expectation) => {
      const annotationJson = this.Factory.annotation({}, annotationType);
      const annotation = this.NumberAnnotation.create(annotationJson, annotationType);
      annotation.value = expectation.value;

      expect(annotation.isValueValid()).toEqual(expectation.expected);
    });
  });

  describe('shared behaviour', function() {
    const context = {};

    beforeEach(function() {
      context.classType = this.NumberAnnotation;
      context.annotationTypeJson = this.annotationTypeJson.bind(this);
      context.annotationJson =
        (options, annotationTypeJson) => this.Factory.annotation(options, annotationTypeJson)
      context.createAnnotation = this.NumberAnnotation.create.bind(this.NumberAnnotation);
    });

    annotationSharedBehaviour(context);

  })

});
