/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: breadcrumbs', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.injectDependencies('$rootScope', '$compile');

      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<breadcrumbs></breadcrumbs>',
          undefined,
          'breadcrumbs');
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller).toBeDefined();
  });
});
