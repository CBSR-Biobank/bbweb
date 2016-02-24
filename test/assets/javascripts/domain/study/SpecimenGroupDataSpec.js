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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('SpecimenGroupData', function() {

    var SpecimenGroupData,
        jsonEntities,
        study,
        specimenGroups,
        specimenGroupData,
        testObj;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testUtils, jsonEntities) {
      testUtils.addCustomMatchers();

      SpecimenGroupData = this.$injector.get('SpecimenGroupData');
      jsonEntities = jsonEntities;
      study = jsonEntities.study();

      specimenGroups = _.map(_.range(3), function() {
        return jsonEntities.specimenGroup(study);
      });

      specimenGroupData = _.map(specimenGroups, function(sg) {
        return jsonEntities.specimenGroupData(sg);
      });

      testObj = _.extend({specimenGroupData: specimenGroupData}, SpecimenGroupData);
      testObj.studySpecimenGroups(specimenGroups);
    }));

    function compareSpecimenGroupData(expected, actual) {
      expect(expected.specimenGroupId).toEqual(actual.specimenGroupId);
      expect(expected.maxCount).toEqual(actual.maxCount);
      expect(expected.amount).toEqual(actual.amount);
    }

    it('should return the the correct IDs', function() {
      var allIds = testObj.specimenGroupDataIds();
      expect(allIds).toBeArrayOfSize(specimenGroupData.length);
      expect(allIds).toContainAll(_.pluck(specimenGroupData, 'specimenGroupId'));
    });

    it('getSpecimenGroupDataById should return the correct result', function() {
      compareSpecimenGroupData(testObj.getSpecimenGroupDataById(specimenGroups[0].id), specimenGroupData[0]);
    });

    it('getSpecimenGroupDataById should throw an error for an invalid id', function() {
      var badId = jsonEntities.stringNext();
      expect(function () { testObj.getSpecimenGroupDataById(badId); })
        .toThrow(new Error('specimen group data with id not found: ' + badId));
    });

    it('getSpecimenGroupData should return valid results', function() {
      var result = testObj.getSpecimenGroupData();
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

      expect(testObj.getSpecimenGroupsAsString()).toBe(expectedStrs.join(', '));
    });


  });

});
