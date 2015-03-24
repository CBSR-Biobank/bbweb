/**
 * Jasmine test suite
 *
 * global define
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../annotationTypeDataSetSharedSpec',
  'biobankApp'
], function(angular, mocks, _, annotationTypeDataSetSharedSpec) {
  'use strict';

  describe('CollectionEventType', function() {

    var CollectionEventType,
        cetFromServer,
        fakeEntities,
        study;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(_CollectionEventType_,
                               fakeDomainEntities) {
      CollectionEventType = _CollectionEventType_;
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

    it('should return the correct size for specimen group data', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });
      expect(cet.specimenGroupDataSize()).toBe(cetFromServer.specimenGroupData.length);
    });

    it('should return the specimen group IDs', function() {
      var specimenGroupIds = _.pluck(study.specimenGroups, 'id');

      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });
      expect(cet.allSpecimenGroupDataIds()).toBeArrayOfSize(cetFromServer.specimenGroupData.length);

      _.each(cet.allSpecimenGroupDataIds(), function(id) {
        expect(specimenGroupIds).toContain(id);
      });
    });

    it('should return the specimen group IDs', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { specimenGroups: study.specimenGroups });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studySpecimenGroups: study.specimenGroups });

      _.each(cetFromServer.specimenGroupData, function(serverSgDataItem) {
        var sgDataItem = cet.getSpecimenGroupData(serverSgDataItem.specimenGroupId);
        expect(serverSgDataItem.specimenGroupId).toBe(sgDataItem.specimenGroupId);
        expect(serverSgDataItem.maxCount).toBe(sgDataItem.maxCount);
        expect(serverSgDataItem.amount).toBe(sgDataItem.amount);
      });
    });

    it('should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(function () { cet.getSpecimenGroupData(study.specimenGroups[0].id); })
        .toThrow(new Error('no data items'));
    });

    it('getSpecimenGroupData should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(function () { cet.getSpecimenGroupData(study.specimenGroups[0].id); })
        .toThrow(new Error('no data items'));
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

    it('getSpecimenGroupsAsString should throw an error if there are no specimen group data items', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(function () { cet.getSpecimenGroupsAsString(); })
        .toThrow(new Error('no data items'));
    });

    it('should return the correct size for annotation type data', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { annotationTypes: study.annotationTypes });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studyAnnotationTypes: study.annotationTypes });
      expect(cet.annotationTypeDataSize()).toBe(cetFromServer.annotationTypeData.length);
    });

    it('should return the annotation type IDs', function() {
      var annotationTypeIds = _.pluck(study.annotationTypes, 'id');

      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { annotationTypes: study.annotationTypes });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studyAnnotationTypes: study.annotationTypes });
      expect(cet.allAnnotationTypeDataIds()).toBeArrayOfSize(cetFromServer.annotationTypeData.length);

      _.each(cet.allAnnotationTypeDataIds(), function(id) {
        expect(annotationTypeIds).toContain(id);
      });
    });

    it('should return the annotation type IDs', function() {
      var cetFromServer = fakeEntities.collectionEventType(
        study,
        { annotationTypes: study.annotationTypes });
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        { studyAnnotationTypes: study.annotationTypes });

      _.each(cetFromServer.annotationTypeData, function(serverAtDataItem) {
        var atDataItem = cet.getAnnotationTypeData(serverAtDataItem.annotationTypeId);
        expect(serverAtDataItem.annotationTypeId).toBe(atDataItem.annotationTypeId);
        expect(serverAtDataItem.maxCount).toBe(atDataItem.maxCount);
        expect(serverAtDataItem.amount).toBe(atDataItem.amount);
      });
    });

    it('getAnnotationTypeData throws an error if there are no annotation type data items', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(function () { cet.getAnnotationTypeData(study.annotationTypes[0].id); })
        .toThrow(new Error('no data items'));
    });

    it('returns the collection event type required by the server', function() {
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

      var serverCeventType = cet.getServerCeventType();
      expect(serverCeventType).toEqual(cetFromServer);
    });

    describe('uses annotation type set correctly', function () {

      var study, annotationTypes, cetFromServer, cet;
      var context = {};

      beforeEach(inject(function(CollectionEventType,
                                 fakeDomainEntities) {

        study = fakeDomainEntities.study();
        annotationTypes = _.map(_.range(2), function() {
          return fakeDomainEntities.annotationType(study);
        });

        cetFromServer = fakeEntities.collectionEventType(study, { annotationTypes: annotationTypes});
        cetFromServer.annotationTypeData[0].required = true;
        cetFromServer.annotationTypeData[0].required = false;

        cet = new CollectionEventType(study,
                                      cetFromServer,
                                      { studyAnnotationTypes: annotationTypes });
        context.parentObj = cet;
      }));

      annotationTypeDataSetSharedSpec(context);
    });

  });

});
