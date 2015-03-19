/**
 * Jasmine test suite
 *
 * global define
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('SpecimenGroupSet', function() {

    var SpecimenGroupSet,
        fakeEntities,
        specimenGroups = [
          { id: 'abc', name: 'test1'},
          { id: 'def', name: 'test2'}
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (fakeDomainEntities, _SpecimenGroupSet_) {
      SpecimenGroupSet = _SpecimenGroupSet_;
      fakeEntities = fakeDomainEntities;
    }));

    it('throws an exception if argument is undefined', function() {
      expect(function () { return new SpecimenGroupSet(); })
        .toThrow(new Error('specimenGroups is undefined'));
    });


    it('should return the correct specimen group', function() {
      var set = new SpecimenGroupSet(specimenGroups);
      _.each(specimenGroups, function(expectedSg) {
        var sg = set.get(expectedSg.id);
        expect(sg.id).toBe(expectedSg.id);
        expect(sg.name).toBe(expectedSg.name);
      });
    });

    it('throws exception if ID not found', function() {
      var set = new SpecimenGroupSet(specimenGroups);
      var badId = fakeEntities.stringNext();

      expect(function () { set.get(badId); })
        .toThrow(new Error('specimen group not found: ' + badId));
    });

    it('find returns the correct value', function() {
      var set = new SpecimenGroupSet(specimenGroups);
      expect(set.find(specimenGroups[0].id)).toEqual(specimenGroups[0]);
    });


  });

});
