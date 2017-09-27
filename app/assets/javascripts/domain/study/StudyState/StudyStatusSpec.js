/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash'
], function(angular, mocks, _) {
  'use strict';

  describe('StudyState', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    it('should have values', inject(function (StudyState) {
      expect(_.keys(StudyState)).not.toBeEmptyArray();
    }));
  });

});
