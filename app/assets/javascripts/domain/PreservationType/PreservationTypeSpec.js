/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../index'

describe('PreservationType', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (PreservationType) {
      this.PreservationType = PreservationType;
    });
  });

  it('should have values', function () {
    expect(_.keys(this.PreservationType)).not.toBeEmptyArray();
  });

});
