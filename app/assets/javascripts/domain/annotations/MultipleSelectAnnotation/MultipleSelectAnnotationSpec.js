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
describe('MultipleSelectAnnotation', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(EntityTestSuiteMixin) {
      _.extend(this, EntityTestSuiteMixin);
      this.injectDependencies('MultipleSelectAnnotation',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Factory');
    });
  });

  it('set value can be assigned a string', function() {
    var jsonAnnotationType = this.Factory.annotationType({
          valueType:     this.AnnotationValueType.SELECT,
          maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
          options:       [ 'option1', 'option2' ],
          required:      true
        }),
        jsonAnnotation = this.Factory.annotation({}, jsonAnnotationType),
        annotationType = new this.AnnotationType(jsonAnnotationType),
        annotation     = this.MultipleSelectAnnotation.create(jsonAnnotation, annotationType);

    expect(function () {
      annotation.setValue('option1');
    }).toThrowError('value is not an array');
  });

  it('someSelected returns valid results for multiple select', function() {
    const annotationType = this.AnnotationType.create(
      this.Factory.annotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options:       [ 'option1', 'option2' ],
        required:      true
      }));

    const annotation = this.MultipleSelectAnnotation.create(undefined, annotationType);

    expect(annotationType.options).not.toBeEmptyArray();
    expect(annotation.value).toBeArrayOfSize(annotationType.options.length);
    expect(annotation.someSelected()).toBe(false);

    annotation.value[0].checked = true;
    expect(annotation.someSelected()).toBe(true);
  });

  it('getDisplayValue returns valid results', function() {
    const annotationType = this.AnnotationType.create(
      this.Factory.annotationType({
        valueType:     this.AnnotationValueType.SELECT,
        maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
        options:       [ 'option1', 'option2', 'option3' ],
        required:      true
      }));

    const value = this.Factory.valueForAnnotation(annotationType);
    const serverAnnotation = this.Factory.annotation({ value: value }, annotationType);
    const annotation = this.MultipleSelectAnnotation.create(serverAnnotation, annotationType);

    expect(annotation.getDisplayValue()).toEqual(serverAnnotation.selectedValues.join(', '));
  });

  describe('shared behaviour', function() {
    const context = {};

    beforeEach(function() {
      context.classType = this.MultipleSelectAnnotation;
      context.annotationTypeJson =
        (options = {}) => this.Factory.annotationType(Object.assign(
          {
            valueType:     this.AnnotationValueType.SELECT,
            maxValueCount: this.AnnotationMaxValueCount.SELECT_MULTIPLE,
            options:       [ 'option1', 'option2' ],
            required:      false
          },
          options));
      context.annotationJson =
        (options, annotationTypeJson) => this.Factory.annotation(options, annotationTypeJson)
      context.createAnnotation = this.MultipleSelectAnnotation.create.bind(this.MultipleSelectAnnotation);
    });

    annotationSharedBehaviour(context);

  })

});
