/**
 * Jasmine test suite
 *
 * global define
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('SpecimenGroupSet', function() {

    var specimenGroups = [
      { id: 'abc', name: 'test1'},
      { id: 'def', name: 'test2'}
    ];

    beforeEach(mocks.module('biobankApp'));

    it('should return the correct specimen group', inject(function(SpecimenGroupSet) {
      var set = new SpecimenGroupSet(specimenGroups);
      _.each(specimenGroups, function(expectedSg) {
        var sg = set.get(expectedSg.id);
        expect(sg.id).toBe(expectedSg.id);
        expect(sg.name).toBe(expectedSg.name);
      });
    }));

  });

});
