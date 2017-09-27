/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('annotationTypeSummaryComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);

      this.injectDependencies('$componentController',
                              'AnnotationType',
                              'factory');
    });
  });

  it('can be created', function () {
    this.$componentController(
      'annotationTypeSummary',
      null,
      {
        annotationType: new this.AnnotationType(this.factory.annotationType()),
        test: 'xxx'
      });
  });

});
