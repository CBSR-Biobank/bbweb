/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('annotationTypeSummaryComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
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
