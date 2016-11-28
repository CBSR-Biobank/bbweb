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

  describe('UserState', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    it('should have values', inject(function (UserState) {
      expect(_.keys(UserState)).not.toBeEmptyArray();
    }));
  });

});
