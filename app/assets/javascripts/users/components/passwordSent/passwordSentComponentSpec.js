/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('Component: passwordSent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ComponentTestSuiteMixin) {
      _.extend(this, ComponentTestSuiteMixin.prototype);
      this.injectDependencies('$rootScope', '$compile', 'factory');
      this.email = this.factory.emailNext();

      this.createController = (email) => {
        email = email || this.email;
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<password-sent email="vm.email"></password-sent>',
          { email: email },
          'passwordSent');
      };
    });
  });

  it('has valid scope', function() {
    this.createController();
    expect(this.controller.email).toEqual(this.email);
  });

});
