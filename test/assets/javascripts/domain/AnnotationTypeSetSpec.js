/**
 * Jasmine test suite
 *
 * global define
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('AnnotationTypeSet', function() {

    var annotationTypes = [
      { id: 'abc', name: 'test1'},
      { id: 'def', name: 'test2'}
    ];

    beforeEach(mocks.module('biobankApp'));

    it('should return the correct specimen group', inject(function(AnnotationTypeSet) {
      var set = new AnnotationTypeSet(annotationTypes);
      _.each(annotationTypes, function(expectedSg) {
        var sg = set.get(expectedSg.id);
        expect(sg.id).toBe(expectedSg.id);
        expect(sg.name).toBe(expectedSg.name);
      });
    }));

  });

});
