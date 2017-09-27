/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('SpecimenType', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (SpecimenType) {
      this.SpecimenType = SpecimenType;
    });
  });

  it('should have values', function () {
    expect(_.keys(this.SpecimenType)).not.toBeEmptyArray();
  });

});
