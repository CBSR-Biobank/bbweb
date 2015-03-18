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

  describe('ProcessingTypeSet', function() {

    var ProcessingTypeSet,
        fakeEntities,
        processingTypes = [
          { id: 'abc', name: 'test1'},
          { id: 'def', name: 'test2'}
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (fakeDomainEntities, _ProcessingTypeSet_) {
      ProcessingTypeSet = _ProcessingTypeSet_;
      fakeEntities = fakeDomainEntities;
    }));

    it('throws an exception if argument is undefined', function() {
      expect(function () { new ProcessingTypeSet(); })
        .toThrow(Error('processingTypes is undefined'));
    });


    it('should return the correct processing type', function() {
      var set = new ProcessingTypeSet(processingTypes);
      _.each(processingTypes, function(expectedSg) {
        var sg = set.get(expectedSg.id);
        expect(sg.id).toBe(expectedSg.id);
        expect(sg.name).toBe(expectedSg.name);
      });
    });

    it('throws exception if ID not found', function() {
      var set = new ProcessingTypeSet(processingTypes);
      var badId = fakeEntities.stringNext();

      expect(function () { set.get(badId); })
        .toThrow(Error('processing type not found: ' + badId));
    });

    it('find returns the correct value', function() {
      var set = new ProcessingTypeSet(processingTypes);
      expect(set.find(processingTypes[0].id)).toEqual(processingTypes[0]);
    });


  });

});
