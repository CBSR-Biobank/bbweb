/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { ComponentTestSuiteMixin } from 'test/mixins/ComponentTestSuiteMixin';
import ngModule from '../../index'

describe('Directive: annotationTypeAddDirective', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ComponentTestSuiteMixin);

      this.injectDependencies('$rootScope',
                              '$compile',
                              'AnnotationType',
                              'AnnotationValueType',
                              'AnnotationMaxValueCount',
                              'annotationValueTypeLabelService',
                              'Factory');

      this.onSubmit = jasmine.createSpy('onSubmit');
      this.onCancel = jasmine.createSpy('onCancel');

      this.createController = () => {
        this.createControllerInternal(
          `<annotation-type-add
              on-submit="vm.onSubmit"
              on-cancel="vm.onCancel"
            </annotation-type-add>`,
          {
            onSubmit: this.onSubmit,
            onCancel: this.onCancel
          },
          'annotationTypeAdd');
      };
    });
  });

  it('scope should be valid when adding', function() {
    var valueTypes = Object.values(this.AnnotationValueType),
        labels = this.annotationValueTypeLabelService.getLabels();
    this.createController();
    expect(this.controller.annotationType).toEqual(jasmine.any(this.AnnotationType));
    expect(this.controller.valueTypes.length).toEqual(labels.length);
    labels.forEach(function (label) {
      expect(valueTypes).toContain(label.id);
    });
  });

  it('maxValueCountRequired is valid', function() {
    this.createController();

    this.controller.annotationType.valueType = this.AnnotationValueType.SELECT;

    this.controller.annotationType.maxValueCount = this.AnnotationMaxValueCount.NONE;
    expect(this.controller.maxValueCountRequired()).toBe(true);

    this.controller.annotationType.maxValueCount = this.Factory.stringNext();
    expect(this.controller.maxValueCountRequired()).toBe(true);

    this.controller.annotationType.maxValueCount = this.AnnotationMaxValueCount.SELECT_SINGLE;
    expect(this.controller.maxValueCountRequired()).toBe(false);

    this.controller.annotationType.maxValueCount = this.AnnotationMaxValueCount.SELECT_MULTIPLE;
    expect(this.controller.maxValueCountRequired()).toBe(false);
  });

  it('calling valueTypeChange clears the options array', function() {
    this.createController();

    this.controller.annotationType.valueType = 'Select';
    this.controller.annotationType.maxValueCount = 1;

    this.controller.valueTypeChange();
    expect(this.controller.annotationType.options).toBeArray();
    expect(this.controller.annotationType.options).toBeEmptyArray();
  });

  it('calling optionAdd appends to the options array', function() {
    this.createController();

    this.controller.annotationType.valueType = 'select';
    this.controller.annotationType.maxValueCount = 1;
    this.controller.annotationType.valueTypeChanged();

    this.controller.optionAdd();
    expect(this.controller.annotationType.options).toBeArrayOfSize(1);
    expect(this.controller.annotationType.options).toBeArrayOfStrings();
  });

  it('calling optionRemove throws an error on empty array', function() {
    this.createController();

    this.controller.annotationType.valueType = 'Select';
    this.controller.annotationType.maxValueCount = 1;
    this.controller.annotationType.valueTypeChanged();
    expect(function () { this.controller.optionRemove('abc'); }).toThrow();
  });

  it('calling optionRemove throws an error if removal results in empty array', function() {
    this.createController();

    this.controller.annotationType.valueType = 'Select';
    this.controller.annotationType.maxValueCount = 1;
    this.controller.annotationType.valueTypeChanged();
    this.controller.annotationType.options = ['abc'];
    expect(function () { this.controller.optionRemove('abc'); }).toThrow();
  });

  it('calling optionRemove removes an option', function() {
    this.createController();

    // note: more than two strings in options array
    var options = ['abc', 'def'];
    this.controller.annotationType.valueType = 'Select';
    this.controller.annotationType.maxValueCount = 1;
    this.controller.annotationType.valueTypeChanged();
    this.controller.annotationType.options = options.slice(0);
    this.controller.optionRemove('abc');
    expect(this.controller.annotationType.options).toBeArrayOfSize(options.length - 1);
  });

  it('calling removeButtonDisabled returns valid results', function() {
    this.createController();

    // note: more than two strings in options array
    var options = ['abc', 'def'];
    this.controller.annotationType.valueType = 'Select';
    this.controller.annotationType.maxValueCount = 1;
    this.controller.annotationType.valueTypeChanged();
    this.controller.annotationType.options = options.slice(0);

    expect(this.controller.removeButtonDisabled()).toEqual(false);

    this.controller.annotationType.options = options.slice(1);
    expect(this.controller.removeButtonDisabled()).toEqual(true);
  });

  it('should invoke submit function', function() {
    this.createController();

    var annotType = new this.AnnotationType(this.Factory.annotationType());
    this.controller.submit(annotType);
    expect(this.onSubmit).toHaveBeenCalledWith(annotType);
  });

  it('should invoke cancel function', function() {
    this.createController();
    this.controller.cancel();
    expect(this.onCancel).toHaveBeenCalled();
  });

});
