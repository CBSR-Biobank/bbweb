/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import annotationSharedBehaviour from '../../../test/behaviours/annotationSharedBehaviour';
import ngModule from '../../index'

/*
 * AnnotationSpec.js has test cases for all types of annotations.
 *
 * These test cases provide additional code coverage to the ones in AnnotationSpec.js.
 */
describe('SingleSelectAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin);
      this.injectDependencies('SingleSelectAnnotation',
                              'AnnotationType',
                              'AnnotationMaxValueCount',
                              'AnnotationValueType',
                              'Factory');
      this.createAnnotationType = (options) => {
        const annotationType = this.AnnotationType.create(
          this.Factory.annotationType(
            Object.assign(
              {
                valueType:     this.AnnotationValueType.SELECT,
                maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
                options:       [ 'option1', 'option2' ],
                required:      true
              },
              options)));
        return annotationType;
      };
    });
  });

  it('constructor throws exception if value is invalid', function() {
    const annotationType = this.createAnnotationType();
    expect(
      () => new this.SingleSelectAnnotation({ value: 1 }, annotationType)
    ).toThrowError(/invalid value for selected values/);
  });

  it('getValue returns valid results for SINGLE SELECT', function() {
    const annotationType = this.createAnnotationType();
    const value = this.Factory.valueForAnnotation(annotationType);
    const annotationJson = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.SingleSelectAnnotation.create(annotationJson, annotationType);

    expect(annotation.getValue()).toEqual(annotationJson.selectedValues[0]);
  });

  describe('shared behaviour', function() {
    const context = {};

    beforeEach(function() {
      context.classType = this.SingleSelectAnnotation;
      context.annotationTypeJson =
        (options = {}) => this.Factory.annotationType(Object.assign(
          {
            valueType:     this.AnnotationValueType.SELECT,
            maxValueCount: this.AnnotationMaxValueCount.SELECT_SINGLE,
            options:       [ 'option1', 'option2' ],
            required:      false
          },
          options))
      context.annotationJson =
        (options, annotationTypeJson) => this.Factory.annotation(options, annotationTypeJson)
      context.createAnnotation = this.SingleSelectAnnotation.create.bind(this.SingleSelectAnnotation);
    });

    annotationSharedBehaviour(context);

  })

});
