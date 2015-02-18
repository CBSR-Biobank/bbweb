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
  'biobank.fakeDomainEntities',
  'biobank.annotationTypeDataSetCommon',
  'biobankApp'
], function(angular, mocks, _, faker, fakeEntities, commonTests) {
  'use strict';

  describe('CollectionEventType', function() {

    var CollectionEventType, SpecimenGroupSet, AnnotationTypeSet, cetFromServer;

    var study = fakeEntities.study();

    study.specimenGroups = _.map(_.range(2), function() {
      return fakeEntities.specimenGroup(study);
    });

    study.annotationTypes = _.map(_.range(2), function() {
      return fakeEntities.annotationType(study);
    });

    study.specimenGroupsById = _.indexBy(study.specimenGroups, 'id');
    study.annotationTypesById = _.indexBy(study.annotationTypes, 'id');

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_CollectionEventType_, _SpecimenGroupSet_, _AnnotationTypeSet_) {
      CollectionEventType = _CollectionEventType_;
      SpecimenGroupSet    = _SpecimenGroupSet_;
      AnnotationTypeSet   = _AnnotationTypeSet_;

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

    it('calling addSpecimenGroupData adds a new specimen group data item', function() {
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studySpecimenGroups: study.specimenGroups });
      var newSgDataItem = fakeEntities.specimenGroupData(study.specimenGroups[0]);
      var sgDataItem;

      var originalSgDataItemCount = cet.specimenGroupDataSize();
      cet.addSpecimenGroupData(newSgDataItem);
      expect(cet.specimenGroupDataSize()).toBe(originalSgDataItemCount + 1);

      sgDataItem = cet.getSpecimenGroupData(newSgDataItem.specimenGroupId);
      expect(sgDataItem.specimenGroup).toEqual(study.specimenGroups[0]);
      expect(_.omit(sgDataItem, 'specimenGroup')).toEqual(newSgDataItem);
    });

    it('allows adding multiple specimen group data items with empty id', function() {
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studySpecimenGroups: study.specimenGroups });
      var noIdSgDataItem = {specimenGroupId: '', maxCount: 0, amount: 0};

      var originalSgDataItemCount = cet.specimenGroupDataSize();

      var numNewItems = 5;
      _.each(_.range(numNewItems), function() { cet.addSpecimenGroupData(noIdSgDataItem); });

      expect(cet.specimenGroupDataSize()).toBe(originalSgDataItemCount + numNewItems);
    });

    it('allow removing specimen group data items', function() {
      var cetFromServer = fakeEntities.collectionEventType(study, { specimenGroups: study.specimenGroups});
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studySpecimenGroups: study.specimenGroups });
      var sgDataItemCount = cet.specimenGroupDataSize();

      _.each(cet.allSpecimenGroupDataIds(), function(id) {
        cet.removeSpecimenGroupData(id);
        sgDataItemCount = sgDataItemCount - 1;
        expect(cet.specimenGroupDataSize()).toBe(sgDataItemCount);
      });
    });

    it('calling addAnnotationTypeData adds a new annotation type data item', function() {
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studyAnnotationTypes: study.annotationTypes });
      var newAtDataItem = fakeEntities.annotationTypeData(study.annotationTypes[0]);
      commonTests.addItem(cet, newAtDataItem, study.annotationTypes[0]);
    });


    it('allows adding multiple annotation type data items with empty id', function() {
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studyAnnotationTypes: study.annotationTypes });
      var noIdAtDataItem = {annotationTypeId: '', required: false};
      commonTests.addItem(cet, noIdAtDataItem);
    });

    it('allow removing annotation type data items', function() {
      var cetFromServer = fakeEntities.collectionEventType(study, { annotationTypes: study.annotationTypes});
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { studyAnnotationTypes: study.annotationTypes });
      commonTests.removeItems(cet);
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
