// Jasmine test suite
//
define([
  'angular',
  'angularMocks',
  'underscore',
  'moment',
  'biobankApp'
], function(angular, mocks, _, moment) {
  'use strict';

  describe('AnnotationHelper', function() {

    var AnnotationHelper, AnnotationValueType, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_AnnotationHelper_,
                               _AnnotationValueType_,
                               fakeDomainEntities) {
      AnnotationHelper    = _AnnotationHelper_;
      AnnotationValueType = _AnnotationValueType_;
      fakeEntities        = fakeDomainEntities;
    }));

    it('constructor fails if "required" omitted', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      });

      expect(function () { return new AnnotationHelper(annotationType); })
        .toThrow(new Error('required not assigned'));
    });

    it('constructor successful if annotation type has "required" attribute', function() {
      var annotationType = _.extend({required: true}, fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      }));

      expect(function () { return new AnnotationHelper(annotationType); })
        .not.toThrow(new Error('required not assigned'));
    });

    it('constructor fails if value type is invalid', function() {
      var annotationType = fakeEntities.annotationType({valueType: 'ABCDEF'});
      expect(function () { return new AnnotationHelper(annotationType, true); })
        .toThrow(new Error('value type is invalid: ABCDEF'));
    });

    it('constructor fails if max value count is invalid', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT(),
        maxValueCount: 0
      });
      expect(function () { return new AnnotationHelper(annotationType, true); })
        .toThrow(new Error('invalid value for max count'));

      annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT(),
        maxValueCount: 3
      });
      expect(function () { return new AnnotationHelper(annotationType, true); })
        .toThrow(new Error('invalid value for max count'));
    });

    it('valid value attribute for Text, Number, and Select single annotation type', function() {
      var annotatationValueTypes = [
        AnnotationValueType.TEXT(),
        AnnotationValueType.NUMBER(),
        AnnotationValueType.SELECT()
      ];

      _.each(annotatationValueTypes, function (annotatationValueType) {
        var annotationType = fakeEntities.annotationType({valueType: annotatationValueType });
        var helper = new AnnotationHelper(annotationType, false);
        expect(helper.value).not.toBeDefined();
      });
    });

    it('valid value attributes for DateTime annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.DATE_TIME()
      });

      var helper = new AnnotationHelper(annotationType, true);
      expect(helper.value.date).toBe(moment().format('YYYY-MM-DD'));
      expect(helper.value.time.format('YYYY-MM-DD h:mm A'))
        .toBe(moment().format('YYYY-MM-DD h:mm A'));
    });

    it('valid values attribute for Select annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT(),
        maxValueCount: 2
      });
      expect(annotationType.maxValueCount).toBe(2);
      expect(annotationType.options).not.toBeEmptyArray();

      var helper = new AnnotationHelper(annotationType, true);
      expect(helper.values).toBeArrayOfObjects();
      expect(helper.values).toBeArrayOfSize(annotationType.options.length);

      var valueNames = _.pluck(helper.values, 'name');

      _.each(annotationType.options, function (option) {
        expect(valueNames).toContain(option);
      });

      _.each(helper.values, function(selectValue){
        expect(selectValue.checked).toBeFalse();
      });
    });

    it('calling getAnnotationTypeId gives a valid result', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      });

      var helper = new AnnotationHelper(annotationType, true);
      expect(helper.getAnnotationTypeId()).toBe(annotationType.id);
    });

    it('calling getLabel returns the annotation types name', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      });

      var helper = new AnnotationHelper(annotationType, false);
      expect(helper.getLabel()).toBe(annotationType.name);
    });

    it('calling setValue on Text annotation type sets the value correctly', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var value = fakeEntities.domainEntityNameNext(fakeEntities.ENTITY_NAME_ANNOTATION_TYPE());
      helper.setValue({ stringValue: value });
      expect(helper.value).toBe(value);
      expect(helper.displayValue).toBe(value);
    });

    it('calling setValue on Number annotation type sets the value correctly', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.NUMBER()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var value = 123.456;
      helper.setValue({ numberValue: value.toString() });
      expect(helper.value).toBe(value);
      expect(helper.displayValue).toBe(value);
    });

    it('calling setValue on DateTime annotation type sets the value correctly', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.DATE_TIME()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var date = moment('2000-01-01T09:00:00-0700');
      var value = date.format();
      helper.setValue({ stringValue: value });
      expect(helper.value.date).toBe(date.format('YYYY-MM-DD'));
      expect(helper.value.time.format('h:mm A')).toEqual(date.format('h:mm A'));
      expect(helper.displayValue).toStartWith(date.format('YYYY-MM-DD h:mm A'));
    });

    it('getAnnotation returns a valid value for a Text annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.TEXT()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var value = fakeEntities.domainEntityNameNext(fakeEntities.ENTITY_NAME_ANNOTATION_TYPE());
      helper.setValue({ stringValue: value });

      var annotation = helper.getAnnotation();
      expect(annotation.annotationTypeId).toEqual(annotationType.id);
      expect(annotation.selectedValues).toBeEmptyArray();
      expect(annotation.stringValue).toBe(value);
    });

    it('getAnnotation returns a valid value for a Number annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.NUMBER()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var value = 123.456;
      helper.setValue({ numberValue: value.toString() });

      var annotation = helper.getAnnotation();
      expect(annotation.annotationTypeId).toEqual(annotationType.id);
      expect(annotation.selectedValues).toBeEmptyArray();
      expect(annotation.numberValue).toBe(value.toString());
    });

    it('getAnnotation returns a valid value for a DateTime annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.DATE_TIME()
      });

      var helper = new AnnotationHelper(annotationType, false);
      var date = moment('2000-01-01T09:00:00-0700');
      var value = date.format();
      helper.setValue({ stringValue: value });

      var annotation = helper.getAnnotation();
      expect(annotation.annotationTypeId).toEqual(annotationType.id);
      expect(annotation.selectedValues).toBeEmptyArray();
      expect(annotation.stringValue).toBe(value);
    });

    it('getAnnotation returns null for a Select single annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT()
      });

      var helper = new AnnotationHelper(annotationType, false);
      helper.setValue({
        selectedValues: [ { value: annotationType.options[0] } ]
      });

      var annotation = helper.getAnnotation();
      expect(annotation.annotationTypeId).toBe(annotationType.id);
      expect(annotation.selectedValues).toBeArrayOfSize(1);
      expect(annotation.selectedValues[0].annotationTypeId).toBe(annotationType.id);
      expect(annotation.selectedValues[0].value).toBe(annotationType.options[0]);
      expect(annotation.stringValue).toBeUndefined();
    });

    it('getAnnotation returns null for a Select multiple annotation type', function() {
      var annotationType = fakeEntities.annotationType({
        valueType: AnnotationValueType.SELECT(),
        maxValueCount: 2
      });

      var helper = new AnnotationHelper(annotationType, false);
      helper.setValue({
        selectedValues: [
          { value: annotationType.options[0] },
          { value: annotationType.options[1] }
        ]
      });

      console.log(annotationType.options);

      var annotation = helper.getAnnotation();
      expect(annotation.annotationTypeId).toBe(annotationType.id);
      expect(annotation.selectedValues).toBeArrayOfSize(2);
      expect(annotation.stringValue).toBeUndefined();

      expect(annotation.selectedValues[0].annotationTypeId).toBe(annotationType.id);
      expect(annotation.selectedValues[0].value).toBe(annotationType.options[0]);

      expect(annotation.selectedValues[1].annotationTypeId).toBe(annotationType.id);
      expect(annotation.selectedValues[1].value).toBe(annotationType.options[1]);
    });

  });

});
