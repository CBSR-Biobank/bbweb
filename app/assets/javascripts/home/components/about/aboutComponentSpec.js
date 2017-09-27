/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: about', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$rootScope', '$compile');

      this.createController = () =>
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<about></about>',
          undefined,
          'about');
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller).toBeDefined();
  });
});
