/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('AnnotationType', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'Factory');
    });
  });

  it('invalid objects are reported', function () {
    var self = this,
        annotationTypeJson = self.Factory.annotationType({
          valueType: self.AnnotationValueType.SELECT,
          options: []
        });

    var fields = [ 'id', 'name', 'valueType', 'required' ];

    fields.forEach((field) => {
      var jsonMissingField = _.omit(annotationTypeJson, field);
      var validation = self.AnnotationType.isValid(jsonMissingField);
      expect(validation.valid).toEqual(false);
      expect(validation.message).toContain('Missing required property');
    });
  });

  it('has default values', function() {
    var annotationType = new this.AnnotationType();

    expect(annotationType.id).toBeUndefined();
    expect(annotationType.name).toBeUndefined();
    expect(annotationType.description).toBeUndefined();
    expect(annotationType.valueType).toBeUndefined();
    expect(annotationType.maxValueCount).toBeUndefined();
    expect(annotationType.options).toBeUndefined();
  });

  it('create fails for invalid JSON', function () {
    var self = this,
        annotationTypeJson = self.Factory.annotationType({
          valueType: self.AnnotationValueType.SELECT,
          options: []
        });

    var fields = [ 'id', 'name', 'valueType', 'required' ];

    fields.forEach((field) => {
      var jsonMissingField = _.omit(annotationTypeJson, field);
      expect(function () { self.AnnotationType.create(jsonMissingField); })
        .toThrowError(/invalid object from server/);
    });
  });

  it('valueType predicates return valid results', function() {
    var self = this;

    _.values(self.AnnotationValueType).forEach((valueType) => {
      var annotationType = new self.AnnotationType(
        self.Factory.annotationType({ valueType: valueType }));

      expect(annotationType.isValueTypeText()).toBe(valueType === self.AnnotationValueType.TEXT);
      expect(annotationType.isValueTypeNumber()).toBe(valueType === self.AnnotationValueType.NUMBER);
      expect(annotationType.isValueTypeDateTime()).toBe(valueType === self.AnnotationValueType.DATE_TIME);
      expect(annotationType.isValueTypeSelect()).toBe(valueType === self.AnnotationValueType.SELECT);
    });
  });

  it('isSingleSelect returns valid results', function() {
    var self = this;

    _.range(4).forEach((maxValueCount) => {
      var annotationType = new self.AnnotationType(
        self.Factory.annotationType({
          valueType: self.AnnotationValueType.SELECT,
          maxValueCount: maxValueCount
        }));

      expect(annotationType.isSingleSelect()).toBe(
        (maxValueCount === self.AnnotationMaxValueCount.SELECT_SINGLE));
    });
  });

  it('isMultipleSelect returns valid results', function() {
    var self = this;

    _.range(4).forEach((maxValueCount) => {
      var annotationType = new self.AnnotationType(
        self.Factory.annotationType({
          valueType: self.AnnotationValueType.SELECT,
          maxValueCount: maxValueCount
        }));

      expect(annotationType.isMultipleSelect()).toBe(
        (maxValueCount === self.AnnotationMaxValueCount.SELECT_MULTIPLE));
    });
  });

  it('isMaxValueCountValid returns valid results', function() {
    var self = this, annotationType;

    _.range(4).forEach((maxValueCount) => {
      annotationType = new self.AnnotationType(
        self.Factory.annotationType({
          valueType: self.AnnotationValueType.SELECT,
          maxValueCount: maxValueCount
        }));

      expect(annotationType.isMaxValueCountValid()).toBe(
        (maxValueCount === self.AnnotationMaxValueCount.SELECT_SINGLE) ||
          (maxValueCount === self.AnnotationMaxValueCount.SELECT_MULTIPLE));
    });

    _.range(4).forEach((maxValueCount) => {
      annotationType = new self.AnnotationType(
        self.Factory.annotationType({
          valueType: self.AnnotationValueType.TEXT,
          maxValueCount: maxValueCount
        }));

      expect(annotationType.isMaxValueCountValid())
        .toBe(maxValueCount === self.AnnotationMaxValueCount.NONE);
    });
  });

  it('addOption throws an error if value type is not SELECT', function() {
    var self = this,
        valueTypesNoSelect = _.reject(_.values(self.AnnotationValueType), function (valueType) {
          return valueType === self.AnnotationValueType.SELECT;
        });

    valueTypesNoSelect.forEach((valueType) => {
      var annotationType = new self.AnnotationType(
        self.Factory.annotationType({ valueType: valueType }));

      expect(function () { annotationType.addOption(); })
        .toThrow(new Error('value type is not select: ' + valueType));
    });
  });

  it('addOption adds an item to the options array', function() {
    var self = this,
        annotationType = new self.AnnotationType(
          self.Factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: []
          }));
    expect(annotationType.options).toBeArrayOfSize(0);
    annotationType.addOption();
    expect(annotationType.options).toBeArrayOfSize(1);
    expect(annotationType.options[0]).toBe('');
  });

  it('removeOption throws an error if options array is empty', function() {
    var self = this,
        annotationType = new self.AnnotationType(
          self.Factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: []
          }));

    expect(function () { annotationType.removeOption('abc'); })
      .toThrow(new Error('options is empty, cannot remove any more options'));
  });

  it('removeOption removes an item to the options array', function() {
    var self = this,
        options = ['option1', 'option2'],
        annotationType = new self.AnnotationType(
          self.Factory.annotationType({
            valueType: self.AnnotationValueType.SELECT,
            options: options.slice()
          }));
    expect(annotationType.options).toBeArrayOfSize(options.length);
    annotationType.removeOption(0);
    expect(annotationType.options).toBeArrayOfSize(options.length - 1);
    expect(annotationType.options[0]).toContain(options[1]);
  });

  describe('calling valueTypeChanged', function() {

    it('clears the options array', function() {
      var annotationTypeJson = this.Factory.annotationType({
        valueType: this.AnnotationValueType.SELECT,
        options: ['opt1', 'opt2']
      }),
          annotationType = this.AnnotationType.create(annotationTypeJson);

      annotationType.valueType = this.AnnotationValueType.TEXT;
      annotationType.valueTypeChanged();
      expect(annotationType.options).toBeEmptyArray();
    });

    it('max value count is set to NONE', function() {
      var annotationTypeJson = this.Factory.annotationType({
        valueType: this.AnnotationValueType.SELECT,
        options: ['opt1', 'opt2']
      }),
          annotationType = this.AnnotationType.create(annotationTypeJson);

      annotationType.valueType = this.AnnotationValueType.TEXT;
      annotationType.valueTypeChanged();
      expect(annotationType.maxValueCount).toBe(this.AnnotationMaxValueCount.NONE);
    });

  });

});
