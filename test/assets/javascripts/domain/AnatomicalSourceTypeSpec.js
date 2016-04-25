/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore'
], function(angular, mocks, _) {
  'use strict';

  describe('AnatomicalSourceType', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (testUtils, AnatomicalSourceType) {
      this.AnatomicalSourceType = AnatomicalSourceType;
    }));

    it('should have values', function() {
      expect(_.keys(this.AnatomicalSourceType)).not.toBeEmptyArray();
    });

  });

});
