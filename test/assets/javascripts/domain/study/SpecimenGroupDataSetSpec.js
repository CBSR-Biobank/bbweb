/**
 * Jasmine test suite
 *
 * global define
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, testUtils) {
  'use strict';

  describe('SpecimenGroupDataSet', function() {

    var SpecimenGroupDataSet,
        fakeEntities,
        study,
        specimenGroups,
        specimenGroupData,
        theSet;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (fakeDomainEntities, _SpecimenGroupDataSet_) {
      testUtils.addCustomMatchers();

      SpecimenGroupDataSet = _SpecimenGroupDataSet_;
      fakeEntities = fakeDomainEntities;
      study = fakeEntities.study();

      specimenGroups = _.map(_.range(3), function() {
        return fakeEntities.specimenGroup(study);
      });

      specimenGroupData = _.map(specimenGroups, function(sg) {
        return fakeEntities.specimenGroupData(sg);
      });

      theSet = new SpecimenGroupDataSet(specimenGroupData, {
        studySpecimenGroups: specimenGroups
      });
    }));

    function compareSpecimenGroupData(expected, actual) {
      expect(expected.specimenGroupId).toEqual(actual.specimenGroupId);
      expect(expected.maxCount).toEqual(actual.maxCount);
      expect(expected.amount).toEqual(actual.amount);
    }

    it('should return the correct size', function() {
      expect(theSet.size()).toBe(specimenGroupData.length);
    });

    it('should return the the correct IDs', function() {
      var allIds = theSet.allIds();
      expect(allIds).toBeArrayOfSize(specimenGroupData.length);
      expect(allIds).toContainAll(_.pluck(specimenGroupData, 'specimenGroupId'));
    });

    it('get should return the correct result', function() {
      compareSpecimenGroupData(theSet.get(specimenGroups[0].id), specimenGroupData[0]);
    });

    it('get should throw an error for an invalid id', function() {
      var badId = fakeEntities.stringNext();
      expect(function () {
        theSet.get(badId);
      }).toThrow(new Error('specimen group data with id not found: ' + badId));
    });

    it('getSpecimenGroupData should return valid results', function() {
      var result = theSet.getSpecimenGroupData();
      _.each(result, function(item) {
        var sgDataItem = _.findWhere(specimenGroupData, { specimenGroupId: item.specimenGroupId });
        expect(sgDataItem).toBeDefined();
        compareSpecimenGroupData(item, sgDataItem);
      });
    });

    it('getAsString returns valid results', function() {
      var expectedStrs = _.map(specimenGroups, function(sg) {
        var sgDataItem = _.findWhere(specimenGroupData, { specimenGroupId: sg.id });
        return sg.name + ' (' + sgDataItem.maxCount + ', ' + sgDataItem.amount + ' ' +
          sg.units + ')';
      });

      expect(theSet.getAsString()).toBe(expectedStrs.join(', '));
    });


  });

});
