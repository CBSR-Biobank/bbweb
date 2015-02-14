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
  'biobankApp'
], function(angular, mocks, _, faker, fakeEntities) {
  'use strict';

  describe('CollectionEventType', function() {

    var CollectionEventType, SpecimenGroupSet, AnnotationTypeSet, cetFromServer;

    var study = fakeEntities.study();

    var studySpecimenGroups = _.map(_.range(2), function() {
      return fakeEntities.specimenGroup(study);
    });

    var studyAnnotationTypes = _.map(_.range(2), function() {
      return fakeEntities.annotationType(study);
    });

    var specimenGroupsById = _.indexBy(studySpecimenGroups, 'id');
    var annotationTypesById = _.indexBy(studyAnnotationTypes, 'id');

    beforeEach(mocks.module('biobankApp'));

    beforeEach(inject(function(_CollectionEventType_, _SpecimenGroupSet_, _AnnotationTypeSet_) {
      CollectionEventType = _CollectionEventType_;
      SpecimenGroupSet    = _SpecimenGroupSet_;
      AnnotationTypeSet   = _AnnotationTypeSet_;

      cetFromServer = fakeEntities.collectionEventType(
        study,
        {
          specimenGroups: studySpecimenGroups,
          annotationTypes: studyAnnotationTypes
        });
    }));

    it('isNew should be true for a collection event with no ID', function() {
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
      expect(cet.studyId).toBe(study.id);
    });

    it('isNew should be false for a collection event that has an ID', function() {
      var cet = new CollectionEventType(study, cetFromServer);
      expect(cet.study).toEqual(study);
    });

    it('should be initialized with specimen group and annotation type objects', function() {
      var specimenGroupSet = new SpecimenGroupSet(studySpecimenGroups);
      var annotationTypeSet = new AnnotationTypeSet(studyAnnotationTypes);
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        {
          specimenGroupSet: specimenGroupSet,
          annotationTypeSet: annotationTypeSet
        });

      _.each(cet.specimenGroupData, function(sgItem) {
        expect(sgItem.specimenGroup).toEqual(specimenGroupsById[sgItem.specimenGroupId]);
      });

      _.each(cet.annotatinTypeData, function(atItem) {
        expect(atItem.annotationType).toEqual(annotationTypesById[atItem.annotationTypeId]);
      });
    });

    it('should be initialized with specimen group and annotation type objects', function() {
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        {
          specimenGroups: studySpecimenGroups,
          annotationTypes: studyAnnotationTypes
        });

      _.each(cet.specimenGroupData, function(sgItem) {
        expect(sgItem.specimenGroup).toEqual(specimenGroupsById[sgItem.specimenGroupId]);
      });

      _.each(cet.annotatinTypeData, function(atItem) {
        expect(atItem.annotationType).toEqual(annotationTypesById[atItem.annotationTypeId]);
      });
    });

    it('should be initialized with SpecimenGroupSet and AnnotationTypeSet', function() {
      var cet = new CollectionEventType(
        study,
        cetFromServer,
        {
          specimenGroupSet: new SpecimenGroupSet(studySpecimenGroups),
          annotationTypeSet: new AnnotationTypeSet(studyAnnotationTypes)
        });

      _.each(cet.specimenGroupData, function(sgItem) {
        expect(sgItem.specimenGroup).toEqual(specimenGroupsById[sgItem.specimenGroupId]);
      });

      _.each(cet.annotatinTypeData, function(atItem) {
        expect(atItem.annotationType).toEqual(annotationTypesById[atItem.annotationTypeId]);
      });
    });

    it('calling addSpecimenGroupData adds a new specimen group data item', function() {
      var newSgDataItem = fakeEntities.specimenGroupData(fakeEntities.specimenGroup(study));

      var cet = new CollectionEventType(study, cetFromServer);
      var originalSgDataItems = cet.specimenGroupData.length;
      cet.addSpecimenGroupData(newSgDataItem);
      expect(cet.specimenGroupData.length).toBe(originalSgDataItems + 1);

      expect(_.find(cet.specimenGroupData, function(sgItem) {
        return _.isEqual(sgItem, newSgDataItem);
      })).toBeDefined();
    });

    it('allows adding multiple specimen group data items with empty id', function() {
      var filteredItems;
      var noIdSgDataItem = {specimenGroupId: '', maxCount: 0, amount: 0};

      var cet = new CollectionEventType(study, cetFromServer);
      var originalSgDataItems = cet.specimenGroupData.length;

      var numNewItems = 5;
      _.each(_.range(numNewItems), function() { cet.addSpecimenGroupData(noIdSgDataItem); });

      expect(cet.specimenGroupData.length).toBe(originalSgDataItems + numNewItems);

      filteredItems = _.filter(cet.specimenGroupData, function(sgItem) {
        return _.isEqual(sgItem, noIdSgDataItem);
      });

      expect(filteredItems).toBeDefined(numNewItems);
    });

    it('allow removing specimen group data items', function() {
      var cet = new CollectionEventType(study, cetFromServer, { specimenGroups: studySpecimenGroups });
      var sgDataItemCount = cet.specimenGroupData.length;

      _.each(_.pluck(cet.specimenGroupData, 'specimenGroupId'), function(id) {
        cet.removeSpecimenGroupData(id);
        sgDataItemCount = sgDataItemCount - 1;
        expect(cet.specimenGroupData.length).toBe(sgDataItemCount);
      });
    });

    it('calling addAnnotationTypeData adds a new annotation type data item', function() {
      var newAtDataItem = fakeEntities.annotationTypeData(fakeEntities.annotationType(study));

      var cet = new CollectionEventType(study, cetFromServer);
      var originalAtDataItems = cet.annotationTypeData.length;
      cet.addAnnotationTypeData(newAtDataItem);
      expect(cet.annotationTypeData.length).toBe(originalAtDataItems + 1);

      expect(_.find(cet.annotationTypeData, function(atItem) {
        return _.isEqual(atItem, newAtDataItem);
      })).toBeDefined();
    });


    it('allows adding multiple annotation type data items with empty id', function() {
      var filteredItems;
      var noIdAtDataItem = {annotationTypeId: '', required: false};

      var cet = new CollectionEventType(study, cetFromServer);
      var originalAtDataItems = cet.annotationTypeData.length;

      var numNewItems = 5;
      _.each(_.range(numNewItems), function() { cet.addAnnotationTypeData(noIdAtDataItem); });

      expect(cet.annotationTypeData.length).toBe(originalAtDataItems + numNewItems);

      filteredItems = _.filter(cet.annotationTypeData, function(sgItem) {
        return _.isEqual(sgItem, noIdAtDataItem);
      });

      expect(filteredItems).toBeDefined(numNewItems);
    });

    it('allow removing annotation type data items', function() {
      var cet = new CollectionEventType(study, cetFromServer, { annotationTypes: studyAnnotationTypes });
      var atDataItemCount = cet.specimenGroupData.length;

      _.each(_.pluck(cet.annotationTypeData, 'annotationTypeId'), function(id) {
        cet.removeAnnotationTypeData(id);
        atDataItemCount = atDataItemCount - 1;
        expect(cet.annotationTypeData.length).toBe(atDataItemCount);
      });
    });

    it('returns specimen group data as a string', function() {
      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { specimenGroups: studySpecimenGroups });
      var str = cet.getSpecimenGroupsAsString();
      var regex = /(\w+) \((\d+), (\d+) (\w+)\)/g;

      var matches = regex.exec(str);
      while (matches !== null) {
        checkSpecimenGroupMatches(cet, matches);
        matches = regex.exec(str);
      }

      function checkSpecimenGroupMatches(cet, matches) {
        var found;

        expect(matches).toBeArrayOfSize(5);

        // find the specimen group data item with the matching name
        found = _.find(cet.specimenGroupData, function (sgDataItem) {
          return sgDataItem.specimenGroup.name === matches[1];
        });

        expect(found).toBeDefined();
        expect(matches[2]).toBe(found.maxCount.toString());
        expect(matches[3]).toBe(found.amount.toString());
        expect(matches[4]).toBe(found.specimenGroup.units);
      }

    });
    it('returns annotation type data as a string', function() {
      // need at least 2 annotation type data items for this test
      expect(cetFromServer.annotationTypeData).toBeArrayOfSize(2);
      cetFromServer.annotationTypeData[0].required = false;
      cetFromServer.annotationTypeData[1].required = true;

      var cet = new CollectionEventType(study,
                                        cetFromServer,
                                        { annotationTypes: studyAnnotationTypes });
      var str = cet.getAnnotationTypesAsString();
      var regex = /(\w+) \((\w+)\)/g;
      var matches = regex.exec(str);

      while (matches !== null) {
        checkAnnotationTypeMatches(cet, matches);
        matches = regex.exec(str);
      }

      function checkAnnotationTypeMatches(cet, matches) {
        var found;

        expect(matches).toBeArrayOfSize(3);

        // find the annotation type data item with the matching name
        found = _.find(cet.annotationTypeData, function (atDataItem) {
          return atDataItem.annotationType.name === matches[1];
        });

        expect(found).toBeDefined();
        if (found.required) {
          expect(matches[2]).toBe('Req');
        } else {
          expect(matches[2]).toBe('N/R');
        }
      }
    });


  });

});
