/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
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
    var AnnotationTypeData, jsonEntities;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_AnnotationTypeData_,
                               jsonEntities) {
      AnnotationTypeData = _AnnotationTypeData_;
      jsonEntities = jsonEntities;
    }));

    function createEntities() {
      var entities = {};

      entities.study = jsonEntities.study();

      entities.annotationTypes = _.map(_.range(2), function() {
        return jsonEntities.annotationType(entities.study);
      });

      entities.annotationTypeData = _.map(entities.annotationTypes, function(at) {
        return jsonEntities.annotationTypeData(at);
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
        context.jsonEntities = jsonEntities;
      });

      annotationTypeDataSharedSpec(context);
    });
  });

});
