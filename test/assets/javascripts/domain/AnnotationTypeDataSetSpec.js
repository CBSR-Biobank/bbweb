/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  './annotationTypeDataSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeDataSharedSpec) {
  'use strict';

  describe('AnnotationTypeData', function() {
    var AnnotationTypeData, fakeEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_AnnotationTypeData_,
                               fakeDomainEntities) {
      AnnotationTypeData = _AnnotationTypeData_;
      fakeEntities = fakeDomainEntities;
    }));

    function createEntities() {
      var entities = {};

      entities.study = fakeEntities.study();

      entities.annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(entities.study);
      });

      entities.annotationTypeData = _.map(entities.annotationTypes, function(at) {
        return fakeEntities.annotationTypeData(at);
      });

      return entities;
    }

    it('getAnnotationTypeData returns an empty array if no data items', function() {
      var testObj = _.extend({ annotationTypeData: []}, AnnotationTypeData);
      expect(testObj.getAnnotationTypeData()).toBeArrayOfSize(0);
    });

    it('getAnnotationTypeDataAsString returns empty string if no data items', function() {
      var testObj = _.extend({ annotationTypeData: []}, AnnotationTypeData);
      expect(testObj.getAnnotationTypeDataAsString()).toBeEmptyString();
    });

    describe('uses annotation type set correctly', function () {
      var context = {};

      beforeEach(function() {
        var entities = createEntities(),
            testObj = _.extend({ annotationTypeData: entities.annotationTypeData},
                               AnnotationTypeData);
        testObj.studyAnnotationTypes(entities.annotationTypes);

        context.parentObj = testObj;
        context.annotationTypes = entities.annotationTypes;
        context.fakeEntities = fakeEntities;
      });

      annotationTypeDataSharedSpec(context);
    });
  });

});
