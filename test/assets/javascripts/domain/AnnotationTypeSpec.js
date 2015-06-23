/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
        var annotationType = new AnnotationType(
          fakeEntities.annotationType({ valueType: valueType }));

        expect(annotationType.isValueTypeText()).toBe(valueType === AnnotationValueType.TEXT());
        expect(annotationType.isValueTypeNumber()).toBe(valueType === AnnotationValueType.NUMBER());
        expect(annotationType.isValueTypeDateTime()).toBe(valueType === AnnotationValueType.DATE_TIME());
        expect(annotationType.isValueTypeSelect()).toBe(valueType === AnnotationValueType.SELECT());
      });
    });

    it('isSingleSelect returns valid results', function() {
      _.each(_.range(4), function (maxValueCount) {
        var annotationType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isSingleSelect()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_SINGLE()));
      });
    });

    it('isSingleMultiple returns valid results', function() {
      _.each(_.range(4), function (maxValueCount) {
        var annotationType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMultipleSelect()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });
    });

    it('isMaxValueCountValid returns valid results', function() {
      var annotationType;

      _.each(_.range(4), function (maxValueCount) {
        annotationType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.SELECT(),
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMaxValueCountValid()).toBe(
          (maxValueCount === AnnotationMaxValueCount.SELECT_SINGLE()) ||
            (maxValueCount === AnnotationMaxValueCount.SELECT_MULTIPLE()));
      });

      _.each(_.range(4), function (maxValueCount) {
        annotationType = new AnnotationType(
          fakeEntities.annotationType({
            valueType: AnnotationValueType.TEXT(),
            maxValueCount: maxValueCount
          }));

        expect(annotationType.isMaxValueCountValid())
          .toBe(maxValueCount === AnnotationMaxValueCount.NONE());
      });
    });

    it('addOption throws an error if value type is not SELECT', function() {
      var valueTypesNoSelect = _.reject(AnnotationValueType.values(), function (valueType) {
        return valueType === AnnotationValueType.SELECT();
      });

      _.each(valueTypesNoSelect, function (valueType) {
        var annotationType = new AnnotationType(
          fakeEntities.annotationType({ valueType: valueType }));

        expect(function () { annotationType.addOption(); })
          .toThrow(new Error('value type is not select: ' + valueType));
      });
    });

    it('addOption adds an item to the options array', function() {
      var annotationType = new AnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: []
        }));
      expect(annotationType.options).toBeArrayOfSize(0);
      annotationType.addOption();
      expect(annotationType.options).toBeArrayOfSize(1);
      expect(annotationType.options[0]).toBe('');
    });

    it('removeOption throws an error if options array is empty', function() {
        var annotationType = new AnnotationType(
          fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: []
          }));

        expect(function () { annotationType.removeOption('abc'); })
          .toThrow(new Error('options is empty, cannot remove any more options'));
    });

    it('removeOption removes an item to the options array', function() {
      var options = ['option1', 'option2'];
      var annotationType = new AnnotationType(
        fakeEntities.annotationType({
          valueType: AnnotationValueType.SELECT(),
          options: options
        }));
      expect(annotationType.options).toBeArrayOfSize(options.length);
      annotationType.removeOption(options[0]);
      expect(annotationType.options).toBeArrayOfSize(options.length - 1);
      expect(annotationType.options[0]).toContain(options[1]);
    });
  });

});
