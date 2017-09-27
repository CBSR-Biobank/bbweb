/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('PreservationType', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (PreservationType) {
      this.PreservationType = PreservationType;
    });
  });

  it('should have values', function () {
    expect(_.keys(this.PreservationType)).not.toBeEmptyArray();
  });

});
