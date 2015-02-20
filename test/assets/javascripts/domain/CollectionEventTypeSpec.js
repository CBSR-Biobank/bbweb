/**
 * Jasmine test suite
 *
 * global define
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'faker',
  'biobank.annotationTypeDataSetCommon',
  'biobankApp'
], function(angular, mocks, _, faker, commonTests) {
  'use strict';

  describe('CollectionEventType', function() {

    var CollectionEventType, SpecimenGroupSet, AnnotationTypeSet, cetFromServer, fakeEntities;
    var study;

    beforeEach(mocks.module('biobankApp', 'biobank.fakeDomainEntities'));

    beforeEach(inject(function(_CollectionEventType_,
                               _SpecimenGroupSet_,
                               _AnnotationTypeSet_,
                               fakeDomainEntities) {
      CollectionEventType = _CollectionEventType_;
      SpecimenGroupSet    = _SpecimenGroupSet_;
      AnnotationTypeSet   = _AnnotationTypeSet_;
      fakeEntities        = fakeDomainEntities;

      study = fakeEntities.study();

      study.specimenGroups = _.map(_.range(2), function() {
        return fakeEntities.specimenGroup(study);
      });

      study.annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });

      study.specimenGroupsById = _.indexBy(study.specimenGroups, 'id');
      study.annotationTypesById = _.indexBy(study.annotationTypes, 'id');

      cetFromServer = fakeEntities.collectionEventType(study);
    }));

    it('isNew should be true for a collection event type with no ID', function() {
      var cetNoId = _.omit(cetFromServer, 'id');
      var cet = new CollectionEventType(study, cetNoId);
      expect(cet.isNew).toBe(true);
    });

    it('study ID matches the study', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(cet.studyId).toBe(study.id);
    });

    it('study matches the study', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(cet.study).toEqual(study);
    });

    it('isNew should be false for a collection event type that has an ID', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(cet.isNew).toBe(false);
    });

    it('should return the correct size for specimen group data', function() {
      // var cetFromServer = fakeEntities.collectionEventType(study,
      //                                                      { specimenGroups: study.specimenGroups });
      // var cet = new CollectionEventType(study,
      //                                   cetFromServer,
      //                                   { studySpecimenGroups: study.specimenGroups });
    });

    it('should be initialized with specimen group and annotation type server objects', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        {
          specimenGroups: study.specimenGroups,
          annotationTypes: study.annotationTypes
        });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        {
          studySpecimenGroups: study.specimenGroups,
          studyAnnotationTypes: study.annotationTypes
        });

      _.each(study.specimenGroups, function(sg) {
        expect(cet.getSpecimenGroupData(sg.id).specimenGroup).toEqual(sg);
      });

      _.each(study.annotationTypes, function(at) {
        expect(cet.getAnnotationTypeData(at.id).annotationType).toEqual(at);
      });
    });

    it('should be initialized with SpecimenGroupSet and AnnotationTypeSet', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        {
          specimenGroups: study.specimenGroups,
          annotationTypes: study.annotationTypes
        });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        {
          studySpecimenGroupSet: new SpecimenGroupSet(study.specimenGroups),
          studyAnnotationTypeSet: new AnnotationTypeSet(study.annotationTypes)
        });

      _.each(study.specimenGroups, function(sg) {
        expect(cet.getSpecimenGroupData(sg.id).specimenGroup).toEqual(sg);
      });

      _.each(study.annotationTypes, function(at) {
        expect(cet.getAnnotationTypeData(at.id).annotationType).toEqual(at);
      });
    });

    it('returns specimen group data as a string', function() {
      var cetFromServer = fakeEntities.collectionEventType(study, { specimenGroups: study.specimenGroups});
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studySpecimenGroups: study.specimenGroups });
      var str = cet.getSpecimenGroupsAsString();
      var regex = /(\w+) \((\d+), (\d+) (\w+)\)/g;

      var matches = regex.exec(str);
      while (matches !== null) {
        checkSpecimenGroupMatches(cet, matches);
        matches = regex.exec(str);
      }

      function getSgDataItemByName(name) {
        var sgDataItems = _.map(cet.allSpecimenGroupDataIds(), function (id) {
          return cet.getSpecimenGroupData(id);
        });
        return _.find(sgDataItems, function(item) { return item.specimenGroup.name === name; });
      }

      function checkSpecimenGroupMatches(cet, matches) {
        var found;

        expect(matches).toBeArrayOfSize(5);

        // find the specimen group data item with the matching name
        found = getSgDataItemByName(matches[1]);
        expect(found).toBeDefined();
        expect(matches[2]).toBe(found.maxCount.toString());
        expect(matches[3]).toBe(found.amount.toString());
        expect(matches[4]).toBe(found.specimenGroup.units);
      }
    });

    it('returns annotation type data as a string', function() {
      var cetFromServer;

      var annotationTypes = _.map(_.range(2), function() {
        return fakeEntities.annotationType(study);
      });

      cetFromServer = fakeEntities.collectionEventType(study, { annotationTypes: annotationTypes});
      cetFromServer.annotationTypeData[0].required = true;
      cetFromServer.annotationTypeData[0].required = false;

      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studyAnnotationTypes: annotationTypes });
      commonTests.getAsString(cet);
    });


  });

});
