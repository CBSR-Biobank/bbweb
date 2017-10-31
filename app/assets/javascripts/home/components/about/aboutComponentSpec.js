/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('Component: about', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin);
      this.createController = () =>
        ComponentTestSuiteMixin.createController.call(
          this,
          '<about></about>',
          undefined,
          'about');
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller).toBeDefined();
    expect(this.controller.breadcrumbs).toBeDefined();
  });
});
