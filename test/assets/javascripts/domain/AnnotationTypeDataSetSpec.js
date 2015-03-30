/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('AnnotationTypeDataSet', function() {
    var AnnotationTypeDataSet, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_AnnotationTypeDataSet_,
                              fakeDomainEntities) {
      AnnotationTypeDataSet = _AnnotationTypeDataSet_;
      fakeEntities = fakeDomainEntities;
    }));

    it('get throws an error if no data items', function() {
      var dataSet = new AnnotationTypeDataSet();

      expect(function () { dataSet.get(fakeEntities.stringNext()); })
        .toThrow(new Error('no data items'));
    });

    it('getAnnotationTypeData returns an empty array if no data items', function() {
      var dataSet = new AnnotationTypeDataSet();

      expect(dataSet.getAnnotationTypeData()).toBeArrayOfSize(0);
    });

    it('getAsString throws an error if no data items', function() {
      var dataSet = new AnnotationTypeDataSet();

      expect(function () { dataSet.getAsString(); })
        .toThrow(new Error('no data items'));
    });

    it('get throws an error if not found', function() {
      var annotationTypes = [],
          annotationTypeData = [],
          dataSet;

      _.each(_.range(2), function () {
        var annotationType = fakeEntities.annotationType();
        annotationTypes = annotationTypes.concat(annotationType);
        annotationTypeData = annotationTypeData.concat(fakeEntities.annotationTypeData(annotationType));
      });

      dataSet = new AnnotationTypeDataSet(annotationTypeData, {
        studyAnnotationTypes: annotationTypes
      });

      expect(function () {dataSet.get(fakeEntities.stringNext()); })
        .toThrow(new Error('annotation type data with id not found: string_0'));
    });

    it('get returns an item when it is found', function() {
      var annotationTypes = [],
          annotationTypeData = [],
          dataSet;

      _.each(_.range(2), function () {
        var annotationType = fakeEntities.annotationType();
        annotationTypes = annotationTypes.concat(annotationType);
        annotationTypeData = annotationTypeData.concat(fakeEntities.annotationTypeData(annotationType));
      });

      dataSet = new AnnotationTypeDataSet(annotationTypeData, {
        studyAnnotationTypes: annotationTypes
      });

      _.each(_.zip(annotationTypes, annotationTypeData), function (tuple) {
        var annotationType = tuple[0];
        var annotationTypeDataItem = tuple[1];
        var item = dataSet.get(annotationType.id);
        expect(item.annotationTypeId).toEqual(annotationTypeDataItem.annotationTypeId);
        expect(item.required).toEqual(annotationTypeDataItem.required);
      });

    });

    it('getAnnotationTypeData returns valid results', function() {
      var annotationTypes = [],
          annotationTypeData = [],
          dataSet;

      _.each(_.range(2), function () {
        var annotationType = fakeEntities.annotationType();
        annotationTypes = annotationTypes.concat(annotationType);
        annotationTypeData = annotationTypeData.concat(fakeEntities.annotationTypeData(annotationType));
      });

      dataSet = new AnnotationTypeDataSet(annotationTypeData, {
        studyAnnotationTypes: annotationTypes
      });

      expect(dataSet.getAnnotationTypeData()).toEqual(annotationTypeData);
    });

    it('getAsString returns valid results', function() {
      var annotationTypes = [],
          annotationTypeData = [],
          dataSet,
          strs = [];

      _.each(_.range(2), function () {
        var annotationType = fakeEntities.annotationType();
        annotationTypes = annotationTypes.concat(annotationType);
        annotationTypeData = annotationTypeData.concat(fakeEntities.annotationTypeData(annotationType));
      });

      dataSet = new AnnotationTypeDataSet(annotationTypeData, {
        studyAnnotationTypes: annotationTypes
      });

      strs = _.map(_.zip(annotationTypes, annotationTypeData), function (tuple) {
        var annotationType = tuple[0];
        var annotationTypeDataItem = tuple[1];
        return annotationType.name + (annotationTypeDataItem.required ? ' (Req)' : ' (N/R)');
      });

      expect(dataSet.getAsString()).toEqual(strs.join(', '));
    });

  });

});
