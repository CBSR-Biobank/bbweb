/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('AnatomicalSourceType', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (AnatomicalSourceType) {
      this.AnatomicalSourceType = AnatomicalSourceType;
    });
  });

  it('should have values', function() {
    expect(_.keys(this.AnatomicalSourceType)).not.toBeEmptyArray();
  });

});
