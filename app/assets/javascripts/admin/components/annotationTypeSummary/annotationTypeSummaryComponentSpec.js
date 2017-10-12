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
      _.extend(this, TestSuiteMixin);

      this.injectDependencies('$componentController',
                              'AnnotationType',
                              'Factory');
    });
  });

  it('can be created', function () {
    this.$componentController(
      'annotationTypeSummary',
      null,
      {
        annotationType: new this.AnnotationType(this.Factory.annotationType()),
        test: 'xxx'
      });
  });

});
