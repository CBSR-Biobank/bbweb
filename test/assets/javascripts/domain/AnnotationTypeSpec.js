/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('AnnotationType', function() {
    var AnnotationType,
        AnnotationValueType,
        AnnotationMaxValueCount,
        fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_AnnotationType_,
                               _AnnotationValueType_,
                               _AnnotationMaxValueCount_,
                               fakeDomainEntities) {
      AnnotationType= _AnnotationType_;
      AnnotationValueType      = _AnnotationValueType_;
      AnnotationMaxValueCount  = _AnnotationMaxValueCount_;
      fakeEntities = fakeDomainEntities;
    }));

    it('has default values', function() {
      var annotationType = new AnnotationType();

      expect(annotationType.id).toBeNull();
      expect(annotationType.name).toBeEmptyString();
      expect(annotationType.description).toBeNull();
      expect(annotationType.valueType).toBeEmptyString();
      expect(annotationType.maxValueCount).toBeNull();
      expect(annotationType.options).toBeEmptyArray();
    });

    it('valueType predicates return valid results', function() {
      _.each(AnnotationValueType.values(), function (valueType) {
        var annotType = new AnnotationType(
          fakeEntities.annotationType({ valueType: valueType }));

        expect(annotType.isValueTypeText()).toBe(valueType === AnnotationValueType.TEXT());
        expect(annotType.isValueTypeNumber()).toBe(valueType === AnnotationValueType.NUMBER());
        expect(annotType.isValueTypeDateTime()).toBe(valueType === AnnotationValueType.DATE_TIME());
        expect(annotType.isValueTypeSelect()).toBe(valueType === AnnotationValueType.SELECT());
      });
    });

    it('isSingleSelect returns valid results', function() {
      _.each(_.range(4), function (maxValueCount) {
        var annotType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotType.isSingleSelect()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_SINGLE()));
      });
    });

    it('isSingleMultiple returns valid results', function() {
      _.each(_.range(4), function (maxValueCount) {
        var annotType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotType.isMultipleSelect()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });
    });

    it('isMaxValueCountValid returns valid results', function() {
      var annotType;

      _.each(_.range(4), function (maxValueCount) {
        annotType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotType.isMaxValueCountValid()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_SINGLE()) ||
            (maxValueCount === AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });

      _.each(_.range(4), function (maxValueCount) {
        annotType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.TEXT(),
            maxValueCount: maxValueCount
          }));

        expect(annotType.isMaxValueCountValid())
          .toBe(maxValueCount === AnnotationMaxValueCount.NONE());
      });
    });

    it('addOption throws an error if value type is not SELECT', function() {
      var valueTypesNoSelect = _.reject(AnnotationValueType.values(), function (valueType) {
        return valueType === AnnotationValueType.SELECT();
      });

      _.each(valueTypesNoSelect, function (valueType) {
        var annotType = new AnnotationType(
          fakeEntities.annotationType({ valueType: valueType }));

        expect(function () { annotType.addOption(); })
          .toThrow(new Error('value type is not select: ' + valueType));
      });
    });

    it('addOption adds an item to the options array', function() {
      var annotType = new AnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: []
        }));
      expect(annotType.options).toBeArrayOfSize(0);
      annotType.addOption();
      expect(annotType.options).toBeArrayOfSize(1);
      expect(annotType.options[0]).toBe('');
    });

    it('removeOption throws an error if options array is empty', function() {
        var annotType = new AnnotationType(
          fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: []
          }));

        expect(function () { annotType.removeOption('abc'); })
          .toThrow(new Error('options is empty, cannot remove any more options'));
    });

    it('removeOption removes an item to the options array', function() {
      var options = ['option1', 'option2'];
      var annotType = new AnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: options
        }));
      expect(annotType.options).toBeArrayOfSize(options.length);
      annotType.removeOption(options[0]);
      expect(annotType.options).toBeArrayOfSize(options.length - 1);
      expect(annotType.options[0]).toContain(options[1]);
    });
  });

});
