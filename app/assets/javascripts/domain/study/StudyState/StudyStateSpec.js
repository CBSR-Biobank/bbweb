/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import ngModule from '../../index'

describe('StudyState', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (StudyState) {
      this.StudyState = StudyState;
    });
  });

  it('should have values', function () {
    expect(Object.keys(this.StudyState)).not.toBeEmptyArray();
  });

});
